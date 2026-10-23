package com.husnain.financetracker.model;

// These are the only types of account someone can make.
// CURRENT is a normal everyday bank account, SAVINGS is a savings account and CREDIT is a credit card.
// The client has to send one of these exactly in capitals, anything else gets rejected with a 400 before it reaches the service.
// They get saved in the database as text so the names here have to stay under the 20 character limit on the type column in Account.
public enum AccountType {

    CURRENT,
    SAVINGS,
    CREDIT

}
