package com.husnain.financetracker.service;

import com.husnain.financetracker.dto.BudgetStatusResponse;
import com.husnain.financetracker.dto.CategorySpending;
import com.husnain.financetracker.dto.MonthlySummaryResponse;
import com.husnain.financetracker.model.Budget;
import com.husnain.financetracker.model.TransactionType;
import com.husnain.financetracker.repository.BudgetRepository;
import com.husnain.financetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// This is where the two reports get worked out, ReportController passes everything in here.
// The adding up happens in the database because it's faster than loading every transaction into Java.
// The reports add amounts across all accounts as they are, there's no currency conversion.
@Service
@RequiredArgsConstructor

public class ReportService {

    // Zero with 2 decimal places, used when there's nothing to add up so the JSON still shows 0.00.
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;

    // Total income, total spending, the difference between them and where the money went for one month.
    public MonthlySummaryResponse monthlySummary(Long userId, String month) {

        // Turns 2026-10 into the first and last day of that month, atEndOfMonth knows February can have 28 or 29 days.
        YearMonth yearMonth = Months.parse(month);
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = yearMonth.atEndOfMonth();

        BigDecimal income = orZero(transactionRepository.sumAmount(userId, TransactionType.INCOME, from, to));
        BigDecimal spending = orZero(transactionRepository.sumAmount(userId, TransactionType.EXPENSE, from, to));
        List<CategorySpending> byCategory = transactionRepository.sumAmountByCategory(userId, TransactionType.EXPENSE, from, to);

        // subtract is how you do minus with BigDecimal, you can't use the normal minus sign with it.
        return new MonthlySummaryResponse(yearMonth.toString(), income, spending, income.subtract(spending), byCategory);

    }

    // For each budget in the month it works out the limit, how much was spent, what's left and if they went over.
    public List<BudgetStatusResponse> budgetStatus(Long userId, String month) {

        YearMonth yearMonth = Months.parse(month);
        List<Budget> budgets = budgetRepository.findAllByUserIdAndMonth(userId, yearMonth.toString());

        // No budgets means nothing to report so it skips the spending query altogether.
        if (budgets.isEmpty()) {
            return List.of();

        }

        // Gets the spending per category for the month once, then puts it in a map from category id to total.
        // A map makes it quick to look up each budget's spending instead of searching the list every time.
        Map<Long, BigDecimal> spentByCategory = transactionRepository
                .sumAmountByCategory(userId, TransactionType.EXPENSE, yearMonth.atDay(1), yearMonth.atEndOfMonth())
                .stream()
                .collect(Collectors.toMap(CategorySpending::categoryId, CategorySpending::total));

        return budgets.stream()
                .map(budget -> toStatus(budget, spentByCategory))
                .toList();

    }


    // Builds one line of the budget report
    // If nothing was spent in the category it isn't in the map, so getOrDefault gives back 0.00 instead.
    // compareTo gives back more than 0 when spent is bigger than limit, which is how you compare BigDecimals.
    // Spending exactly the limit doesn't count as over budget.
    private BudgetStatusResponse toStatus(Budget budget, Map<Long, BigDecimal> spentByCategory) {

        BigDecimal limit = budget.getLimitAmount();
        BigDecimal spent = spentByCategory.getOrDefault(budget.getCategory().getId(), ZERO);
        return new BudgetStatusResponse(
                budget.getId(),
                budget.getCategory().getId(),
                budget.getCategory().getName(),
                budget.getMonth(),
                limit,
                spent,
                limit.subtract(spent),
                spent.compareTo(limit) > 0);

    }

    // SUM in the database gives back null when there's nothing to add up so this turns that into 0.00.
    private BigDecimal orZero(BigDecimal value) {

        return value == null ? ZERO : value;

    }
}
