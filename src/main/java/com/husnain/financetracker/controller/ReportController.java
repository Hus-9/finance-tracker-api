package com.husnain.financetracker.controller;

import com.husnain.financetracker.dto.BudgetStatusResponse;
import com.husnain.financetracker.dto.MonthlySummaryResponse;
import com.husnain.financetracker.security.UserPrincipal;
import com.husnain.financetracker.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// This controller handles the report endpoints, it passes everything to ReportService.
// Every endpoint in here starts with /api/reports and they all need ?month=2026-10 on the end.
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor

public class ReportController {

    private final ReportService reportService;

    // GET /api/reports/monthly-summary gives back income, spending, the net and spending per category for the month.
    // month has no default so leaving it out is a 400.
    @GetMapping("/monthly-summary")
    public MonthlySummaryResponse monthlySummary(@AuthenticationPrincipal UserPrincipal user, @RequestParam String month) {

        return reportService.monthlySummary(user.getId(), month);

    }

    // GET /api/reports/budget-status gives back every budget for the month with how much is spent and left.
    @GetMapping("/budget-status")
    public List<BudgetStatusResponse> budgetStatus(@AuthenticationPrincipal UserPrincipal user, @RequestParam String month) {

        return reportService.budgetStatus(user.getId(), month);

    }
}
