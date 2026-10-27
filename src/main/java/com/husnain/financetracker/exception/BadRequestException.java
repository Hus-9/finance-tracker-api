package com.husnain.financetracker.exception;

// We throw this when the request breaks a rule that the validation annotations can't check by themselves.
// Like an INCOME transaction in an EXPENSE category, a page size that's too big, or a from date after the to date.
// GlobalExceptionHandler catches it and sends back a 400 Bad Request with the message.

public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {

        super(message);

    }
}
