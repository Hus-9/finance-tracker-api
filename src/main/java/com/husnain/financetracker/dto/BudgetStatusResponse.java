package com.husnain.financetracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

// This is one line of the budget status report so one budget with how it's going.
// remaining is the limit minus what's been spent to it goes negative once they're over.
// @JsonProperty makes sure the JSON field is called isOverBudget exactly like the brief asks for.
public record BudgetStatusResponse(

        Long budgetId,
        Long categoryId,
        String categoryName,
        String month,
        BigDecimal limit,
        BigDecimal spent,
        BigDecimal remaining,
        @JsonProperty("isOverBudget") boolean isOverBudget

) {
}
