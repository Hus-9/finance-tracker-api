package com.husnain.financetracker.service;

import com.husnain.financetracker.exception.BadRequestException;

import java.time.YearMonth;
import java.util.regex.Pattern;

// A small helper for the month text that budgets and report use, like 2026-10
// It's not public because only the services in this package need it.
final class Months {

    // A 4 digit year, then a month from 01 to 12.
    private static final Pattern FORMAT = Pattern.compile("^\\d{4}-(0[1-9]|1[0-2])$");

    private Months() {

    }

    // Checks the month looks right and turns it into a YearMonth, or throws a 400 if it doesn't.
    // YearMonth is Java's type for a month in a year and it knows things like how many days that month has.
    static YearMonth parse(String month) {

        if (month == null || !FORMAT.matcher(month).matches()) {

            throw new BadRequestException("month must look like 2026-10");

        }
        return YearMonth.parse(month);

    }
}
