package com.husnain.financetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

// This is what the client sends when setting a budget.
public record BudgetRequest(

        // Which of the user's EXPENSE categories the limit is for.
        @NotNull
        Long categoryId,

        // The month the budget is for, the regex means a 4 digit year then a month from 01 to 12.
        @Schema(example = "2026-10")
        @NotBlank @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "must look like 2026-10")
        String month,

        // The spending limit, same rules as a transaction amount so more than 0 with at most 2 decimal places.
        @Schema(example = "200.00")
        @NotNull @Positive @Digits(integer = 10, fraction = 2)
        BigDecimal limitAmount

) {
}
