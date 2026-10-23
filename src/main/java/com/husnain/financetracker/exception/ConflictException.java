package com.husnain.financetracker.exception;

// We throw this when the request itself is fine, but it clashes with what's already in the database.
// Like signing up with an email that's already taken, making a category or budget that already exists or deleting a category that's still being used by transaction
// GlobalExceptionHandler catches it and sends back a 409 Conflict with the message.
// It extends RunTimeException so we can throw it from anywhere without adding throws to every method.
public class ConflictException extends RuntimeException {

    // The message is what the user sees in the error response so it should say exactly what clashed.
    public ConflictException(String message) {

        super(message);

    }
}
