package com.husnain.financetracker.dto;

import com.husnain.financetracker.model.Budget;

import java.math.BigDecimal;

// This is what the API sends back for a budget, it includes the category name so the client can show it straight away.
public record BudgetResponse(Long id, Long categoryId, String categoryName, String month, BigDecimal limitAmount) {

    // Turns a Budget from the database into a BudgetResponse, the category has to be loaded already.
    public static BudgetResponse from(Budget budget) {

        return new BudgetResponse(budget.getId(), budget.getCategory().getId(), budget.getCategory().getName(), budget.getMonth(), budget.getLimitAmount());

    }
}
