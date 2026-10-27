package com.husnain.financetracker.service;

import com.husnain.financetracker.dto.BudgetRequest;
import com.husnain.financetracker.dto.BudgetResponse;
import com.husnain.financetracker.exception.BadRequestException;
import com.husnain.financetracker.exception.ConflictException;
import com.husnain.financetracker.exception.ResourceNotFoundException;
import com.husnain.financetracker.model.Budget;
import com.husnain.financetracker.model.Category;
import com.husnain.financetracker.model.TransactionType;
import com.husnain.financetracker.repository.BudgetRepository;
import com.husnain.financetracker.repository.CategoryRepository;
import com.husnain.financetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.List;

// This is where the logic for budget happens, BudgetController passes everything in here.
@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // Gets the user's budgets, all of them if month is null or just that month if one was sent.
    public List<BudgetResponse> list(Long userId, String month) {

        List<Budget> budgets;
        if (month == null) {

            budgets = budgetRepository.findAllByUserId(userId);

        } else {

            budgets = budgetRepository.findAllByUserIdAndMonth(userId, Months.parse(month).toString());

        }
        return budgets.stream().map(BudgetResponse::from).toList();

    }

    // Sets a new budget for one of the user's categories in one month.
    @Transactional
    public BudgetResponse create(Long userId, BudgetRequest request) {

        // Checks the month first so a bad month never even reaches the database.
        String month = Months.parse(request.month()).toString();

        // The category has to be one of theirs, otherwise it's a 404 like everywhere else.
        Category category = categoryRepository.findByIdAndUserId(request.categoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // A budget is a spending limit so it only makes sense for EXPENSE categories, not something like  Salary.
        if (category.getType() != TransactionType.EXPENSE) {

            throw new BadRequestException("Budgets can only be set for EXPENSE categories");

        }

        // Only one budget per category per month, a second one is a 409.
        if (budgetRepository.existsByUserIdAndCategoryIdAndMonth(userId, category.getId(), month)) {

            throw new ConflictException("You already have a budget for " + category.getName() + " in " + month);

        }

        // setScale makes the limit have exactly 2 decimal places, same as transaction amounts.

        Budget budget = Budget.builder()
                .user(userRepository.getReferenceById(userId))
                .category(category)
                .month(month)
                .limitAmount(request.limitAmount().setScale(2, RoundingMode.UNNECESSARY))
                .build();
        return BudgetResponse.from(budgetRepository.save(budget));

    }
}
