package com.husnain.financetracker.controller;

import com.husnain.financetracker.dto.BudgetRequest;
import com.husnain.financetracker.dto.BudgetResponse;
import com.husnain.financetracker.security.UserPrincipal;
import com.husnain.financetracker.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// This controller handles the budget endpoints, it passes everything to BudgetService.
// Every endpoint in here starts with /api/budgets.
@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor

public class BudgetController {

    private final BudgetService budgetService;

    // GET /api/budgets gives back all the user's budgets or add ?month=2026-10 to only get that month.
    @GetMapping
    public List<BudgetResponse> list(@AuthenticationPrincipal UserPrincipal user, @RequestParam(required = false) String month) {

        return budgetService.list(user.getId(), month);

    }

    // POST /api/budgets sets a budget, 201 Created if it works or 409 if that category already has one that month.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BudgetResponse create(@AuthenticationPrincipal UserPrincipal user, @Valid @RequestBody BudgetRequest request) {

        return budgetService.create(user.getId(), request);

    }
}
