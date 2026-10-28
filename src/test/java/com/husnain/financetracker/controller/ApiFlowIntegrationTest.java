package com.husnain.financetracker.controller;

import com.husnain.financetracker.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Integration tests that go through the API the same way a real user would.
// jsonPath reads a value out of the JSON response, $ is the whole response and $.net is its net field.

class ApiFlowIntegrationTest extends AbstractIntegrationTest {

    @Test
    void aUserCanTrackTheirMoneyFromSignUpToReports() throws Exception {

        String token = newUser();

        // Signing up gave the user the six default categories.
        getAs(token, "/api/categories").andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(6)));

        long account = createAccount(token);
        long salary = categoryId(token, "Salary");
        long food = categoryId(token, "Food");

        createTransaction(token, account, salary, "2500.00", "INCOME", "2026-10-01");
        createTransaction(token, account, food, "120.50", "EXPENSE", "2026-10-03");
        createTransaction(token, account, food, "79.50", "EXPENSE", "2026-10-31");
        // September so it shouldn't count in the October report.
        createTransaction(token, account, food, "999.00", "EXPENSE", "2026-09-30");

        getAs(token, "/api/reports/monthly-summary?month=2026-10")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(2500.0))
                .andExpect(jsonPath("$.totalSpending").value(200.0))
                .andExpect(jsonPath("$.net").value(2300.0));

        // A 150 budget on Food, with 200 spent in October.
        postAs(token, "/api/budgets", Map.of("categoryId", food, "month", "2026-10", "limitAmount", new BigDecimal("150")))
                .andExpect(status().isCreated());

        getAs(token, "/api/reports/budget-status?month=2026-10")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].spent").value(200.0))
                .andExpect(jsonPath("$[0].remaining").value(-50.0))
                .andExpect(jsonPath("$[0].isOverBudget").value(true));

    }

    @Test
    void moneyStaysExactWithNoFloatingPointErrors() throws Exception {

        String token = newUser();
        long account = createAccount(token);
        long food = categoryId(token, "Food");

        // With double 0.1 plus 0.2 is 0.3000000000000004, with BigDecimal and NUMERIC it has to be exactly 0.30
        createTransaction(token, account, food, "0.10", "EXPENSE", "2026-10-01");
        createTransaction(token, account, food, "0.20", "EXPENSE", "2026-10-02");

        getAs(token, "/api/reports/monthly-summary?month=2026-10")
                .andExpect(jsonPath("$.totalSpending").value(0.3));

    }

    @Test
    void errorAlwaysComeBackInTheSameFormat() throws Exception {

        // No token at all gives 401 in our ApiError format.
        mockMvc.perform(MockMvcRequestBuilders.get("/api/accounts"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/accounts"))
                .andExpect(jsonPath("$.timestamp").exists());

        String token = newUser();

        // Bad input gives 400 with a list of every field that was wrong.
        postAs(token, "/api/accounts", Map.of("name", " ", "currency", "gbp"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors", hasItem(startsWith("currency:"))))
                .andExpect(jsonPath("$.errors", hasItem(startsWith("name:"))));

        // A URL that doesn't exist and a missing month both use the same format too.
        getAs(token, "/api/nothing-here").andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
        getAs(token, "/api/reports/monthly-summary").andExpect(status().isBadRequest());

    }
}
