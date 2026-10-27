package com.husnain.financetracker.dto;

import java.math.BigDecimal;

// How much was spent in one category, used in both reports.
// The database query builds these directly so we don't have to convert anything afterwards.
public record CategorySpending(Long categoryId, String categoryName, BigDecimal total) {
}
