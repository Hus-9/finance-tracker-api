package com.husnain.financetracker.dto;

import com.husnain.financetracker.model.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// This is what the client sends when making or updating a category.
public record CategoryRequest(

        // The category name, it can't be empty and can't be longer than 100 characters.
        @Schema(example = "Groceries")
        @NotBlank @Size(max = 100)
        String name,

        // INCOME or EXPENSE, they have to send one of them.
        @NotNull
        TransactionType type

) {
}
