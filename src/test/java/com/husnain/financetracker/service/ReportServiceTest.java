package com.husnain.financetracker.service;

import com.husnain.financetracker.dto.BudgetStatusResponse;
import com.husnain.financetracker.dto.CategorySpending;
import com.husnain.financetracker.dto.MonthlySummaryResponse;
import com.husnain.financetracker.exception.BadRequestException;
import com.husnain.financetracker.model.Budget;
import com.husnain.financetracker.model.Category;
import com.husnain.financetracker.model.TransactionType;
import com.husnain.financetracker.repository.BudgetRepository;
import com.husnain.financetracker.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// Unit tests for the report maths, the repositories are fake so we choose exactly what the database would give back.
@ExtendWith(MockitoExtension.class)

public class ReportServiceTest {

    private static final Long USER_ID = 1L;
    private static final LocalDate OCT_1 = LocalDate.of(2026, 10, 1);
    private static final LocalDate OCT_31 = LocalDate.of(2026, 10, 31);

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void monthlySummaryWorksOutTheNetFromIncomeAndSpending() {

        when(transactionRepository.sumAmount(USER_ID, TransactionType.INCOME, OCT_1, OCT_31))
                .thenReturn(new BigDecimal("2500.00"));
        when(transactionRepository.sumAmount(USER_ID, TransactionType.EXPENSE, OCT_1, OCT_31))
                .thenReturn(new BigDecimal("1200.50"));
        when(transactionRepository.sumAmountByCategory(USER_ID, TransactionType.EXPENSE, OCT_1, OCT_31))
                .thenReturn(List.of(new CategorySpending(2L, "Rent", new BigDecimal("1200.50"))));

        MonthlySummaryResponse summary = reportService.monthlySummary(USER_ID, "2026-10");

        // isEqualByComparingTo checks the value only, so 1299.5 and 1299.50 both pass.
        assertThat(summary.net()).isEqualByComparingTo("1299.50");
        assertThat(summary.spendingByCategory()).hasSize(1);

    }

    @Test
    void monthlySummaryOfAnEmptyMonthIsAllZeros() {

        // Nothing is set up here so the fake sums give back null, like SUM does in the database with no rows.
        MonthlySummaryResponse summary = reportService.monthlySummary(USER_ID, "2026-10");

        assertThat(summary.totalIncome()).isEqualByComparingTo("0");
        assertThat(summary.totalSpending()).isEqualByComparingTo("0");
        assertThat(summary.net()).isEqualByComparingTo("0");

    }

    @Test
    void budgetStatusOnlyFlagsBudgetsThatAreActuallyOver() {

        // Food is over, Transport has nothing spent and Bills is exactly on the limit.
        when(budgetRepository.findAllByUserIdAndMonth(USER_ID, "2026-10")).thenReturn(List.of(
                budget(10L, "Food", "150.00"),
                budget(11L, "Transport", "100.00"),
                budget(12L, "Bills", "80.00")));
        when(transactionRepository.sumAmountByCategory(USER_ID, TransactionType.EXPENSE, OCT_1, OCT_31))
                .thenReturn(List.of(
                        new CategorySpending(10L, "Food", new BigDecimal("200.00")),
                        new CategorySpending(12L, "Bills", new BigDecimal("80.00"))));

        List<BudgetStatusResponse> statuses = reportService.budgetStatus(USER_ID, "2026-10");

        BudgetStatusResponse food = statuses.get(0);
        assertThat(food.remaining()).isEqualByComparingTo("-50.00");
        assertThat(food.isOverBudget()).isTrue();

        BudgetStatusResponse transport = statuses.get(1);
        assertThat(transport.spent()).isEqualByComparingTo("0.00");
        assertThat(transport.isOverBudget()).isFalse();

        BudgetStatusResponse bills = statuses.get(2);
        assertThat(bills.remaining()).isEqualByComparingTo("0.00");
        assertThat(bills.isOverBudget()).isFalse();

    }

    @Test
    void aBadMonthIsRejectedBeforeAnyQueryRuns() {

        assertThatThrownBy(() -> reportService.monthlySummary(USER_ID, "2026-13"))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> reportService.budgetStatus(USER_ID, "October"))
                .isInstanceOf(BadRequestException.class);

        verifyNoInteractions(transactionRepository, budgetRepository);

    }

    // Makes a made up budget for October 2026 with its category.
    private Budget budget(Long categoryId, String categoryName, String limit) {

        Category category = Category.builder().id(categoryId).name(categoryName).type(TransactionType.EXPENSE).build();
        return Budget.builder().id(categoryId).category(category).month("2026-10").limitAmount(new BigDecimal(limit)).build();

    }
}
