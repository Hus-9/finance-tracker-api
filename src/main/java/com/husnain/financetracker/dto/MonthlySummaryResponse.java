package com.husnain.financetracker.dto;

import java.math.BigDecimal;
import java.util.List;

// This is what the monthly summary report sends back.
// net is income minus spending so it's negative if the user spent more than they got that month.
// spendingByCategory is the breakdown of where the money went, biggest first.
public record MonthlySummaryResponse(

        String month,
        BigDecimal totalIncome,
        BigDecimal totalSpending,
        BigDecimal net,
        List<CategorySpending> spendingByCategory

) {
}
