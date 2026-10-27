package com.husnain.financetracker.dto;

import com.husnain.financetracker.model.Transaction;
import com.husnain.financetracker.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

// This is what the API sends back for a transaction.
// It includes the category name as well as the id so the client doesn't need another request just to show it.
public record TransactionResponse(

        Long id,
        Long accountId,
        Long categoryId,
        String categoryName,
        BigDecimal amount,
        TransactionType type,
        String description,
        LocalDate date,
        Instant createdAt

) {

    // Turns a Transaction from the database into a TransactionResponse.
    // The account and category have to be loaded already, which the repository makes sure of.
    public static TransactionResponse from(Transaction transaction) {

        return new TransactionResponse(
                transaction.getId(),
                transaction.getAccount().getId(),
                transaction.getCategory().getId(),
                transaction.getCategory().getName(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getDescription(),
                transaction.getDate(),
                transaction.getCreatedAt());

    }
}
