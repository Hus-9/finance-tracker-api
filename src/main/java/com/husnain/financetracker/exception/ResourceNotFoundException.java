package com.husnain.financetracker.exception;

// We throw this when something someone asked for can't be found, like an account, category or transaction id that doesn't exist.
// It also gets thrown when the thing exists but belongs to a different user because the services only search by id and user id together.
// That way people can't even tell if someone else's data exists, it just looks like it isn't there.
// GlobalExceptionHandler catches it and sends back a 404 Not Found with the message.
// It extends RuntimeException so we can throw it from anywhere without adding throws to every method.
public class ResourceNotFoundException extends RuntimeException {

    // The message says what wasn't found, like Account not found and the user sees it in the error response.
    public ResourceNotFoundException(String message) {

        super(message);

    }
}
