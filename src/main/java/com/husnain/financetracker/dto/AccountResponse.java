package com.husnain.financetracker.dto;

import com.husnain.financetracker.model.Account;
import com.husnain.financetracker.model.AccountType;

import java.time.Instant;

// This is what the API sends back when it returns an account.
// We send this instead of the Account entity itself so we pick exactly what the client sees and stuff like the user it belongs to stays hidden.
// createdAt is an instant, which is just an exact point in time in UTC so it means the same thing whatever timezone the client is in.

public record AccountResponse(Long id, String name, AccountType type, String currency, Instant createdAt) {

    // Turns an Account from the database into an AccountResponse.
    // It's static so you can just call AccountResponse.from(account) without making an object first.
    // Keeping this in one place means every endpoint builds the response the same way
    public static AccountResponse from(Account account) {

        return new AccountResponse(account.getId(), account.getName(), account.getType(), account.getCurrency(), account.getCreatedAt());

    }
}
