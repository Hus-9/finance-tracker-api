package com.husnain.financetracker.model;

// Whether money is coming in or going out.
// It gets used for both categories and transactions so a Salary category is INCOME and a Food category is EXPENSE.
// Using the same enum for both makes it easy to check a transaction matches its category.

public enum TransactionType {

    INCOME,
    EXPENSE

}
