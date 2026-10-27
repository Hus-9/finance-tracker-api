package com.husnain.financetracker.dto;

import com.husnain.financetracker.model.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

// This is what the client sends when making or updating a transaction.
public record TransactionRequest(

        // Which of the user's accounts the money went in or out of.
        @NotNull
        Long accountId,

        // Which of the user's categories it's filed under.
        @NotNull
        Long categoryId,

        // How much money, it has to be more than 0 because the type already says if it's in or out.
        // @Digits means up to 10 digits before the decimal point and 2 after which matches NUMERIC(12,2).
        // So 12.50 is fine but 12.505 gets rejected instead of being rounded without anyone noticing.
        @Schema(example = "12.50")
        @NotNull @Positive @Digits(integer = 10, fraction = 2)
        BigDecimal amount,

        // INCOME or EXPENSE, it has to match the category's type or the service rejects it.
        @NotNull
        TransactionType type,

        // An optional note, it can be left out but it can't be longer than 255 characters.
        @Schema(example = "Tesco weekly note")
        @Size(max = 255)
        String description,

        // The day the money moved, sent as YYYY-MM-DD
        @Schema(example = "2026-10-14")
        @NotNull
        LocalDate date
) {
}
