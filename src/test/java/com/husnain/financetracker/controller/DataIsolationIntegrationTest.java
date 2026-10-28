package com.husnain.financetracker.controller;

import com.husnain.financetracker.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// The most important rule in the whole API, one user can never see or change another user's data.
// Every attempt has to look exactly like asking for something that doesn't exist, so 404 and never 403.

class DataIsolationIntegrationTest extends AbstractIntegrationTest {

    private String alice;
    private String bob;
    private long aliceAccount;
    private long aliceFood;
    private long aliceTransaction;

    // @BeforeEach runs before every test so each test gets a fresh Alice with an account and a transaction and a fresh Bob.
    @BeforeEach
    void setUp() throws Exception {


        alice = newUser();
        bob = newUser();
        aliceAccount = createAccount(alice);
        aliceFood = categoryId(alice, "Food");
        aliceTransaction = createTransaction(alice, aliceAccount, aliceFood, "25.00", "EXPENSE", "2026-10-10");

    }

    @Test
    void bobCantSeeChangeOrDeleteAlicesAccount() throws Exception {

        getAs(bob, "/api/accounts/" + aliceAccount).andExpect(status().isNotFound());
        putAs(bob,"/api/accounts/" + aliceAccount, Map.of("name", "Mine now", "type", "CURRENT", "currency", "GBP"))
                .andExpect(status().isNotFound());
        deleteAs(bob, "/api/accounts/" + aliceAccount).andExpect(status().isNotFound());
        getAs(bob, "/api/accounts").andExpect(jsonPath("$", hasSize(0)));

        // Alice's account is still there and unchanged.
        getAs(alice, "/api/accounts/" + aliceAccount).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Monzo"));

    }

    @Test
    void bobCantSeeChangesOrDeleteAlicesTransaction() throws Exception {

        long bobAccount = createAccount(bob);
        long bobFood = categoryId(bob, "Food");
        Map<String, Object> edit = Map.of("accountId", bobAccount, "categoryId", bobFood, "amount", new BigDecimal("1.00"), "type", "EXPENSE", "date", "2026-10-10");

        getAs(bob, "/api/transactions/" + aliceTransaction).andExpect(status().isNotFound());
        putAs(bob, "/api/transactions/" + aliceTransaction, edit).andExpect(status().isNotFound());
        deleteAs(bob, "/api/transactions/" + aliceTransaction).andExpect(status().isNotFound());
        getAs(bob, "/api/transactions").andExpect(jsonPath("$.totalElements").value(0));

    }

    @Test
    void bobCantUseAlicesAccountOrCategoryForHisOwnTransactions() throws Exception {

        long bobAccount = createAccount(bob);
        long bobFood = categoryId(bob, "Food");

        // Bob's category in Alice's account.
        postAs(bob, "/api/transactions", Map.of("accountId", aliceAccount, "categoryId", bobFood, "amount", new BigDecimal("5.00"), "type", "EXPENSE", "date", "2026-10-11"))
                .andExpect(status().isNotFound());
        // Alice's category in Bob's account
        postAs(bob, "/api/transactions", Map.of("accountId", bobAccount, "categoryId", aliceFood, "amount", new BigDecimal("5.00"), "type", "EXPENSE", "date", "2026-10-11"))
                .andExpect(status().isNotFound());
        // Or a budget on Alice's category
        postAs(bob, "/api/budgets", Map.of("categoryId", aliceFood, "month", "2026-10", "limitAmount", new BigDecimal("10")))
                .andExpect(status().isNotFound());

    }

    @Test
    void reportsOnlyCountYourOwnMoney() throws Exception {

        getAs(alice, "/api/reports/monthly-summary?month=2026-10").andExpect(jsonPath("$.totalSpending").value(25.0));
        getAs(bob, "/api/reports/monthly-summary?month=2026-10").andExpect(jsonPath("$.totalSpending").value(0.0));

    }
}
