package com.husnain.financetracker.exception;

import com.husnain.financetracker.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.time.Instant;
import java.util.List;

// This class catches errors thrown anywhere in the controllers or services and turns them into proper ApiError response.
// It means the services can just throw an exception and not worry about what the response looks like.
// Every error comes back in the same JSON format with the right status code, instead of Spring's default error page.
// @RestControllerAdvice makes it apply to every controller in the app.
// @Slf4j is Lombok giving us a log field so we can write to the logs.
@Slf4j
@RestControllerAdvice

public class GlobalExceptionHandler {

    // When the request breaks one of our own rules, like an INCOME transaction in an EXPENSE category, it sends back 400 Bad Request.
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex, HttpServletRequest request) {

        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);

    }

    // This is a backup for when the database itself refuses something, like the unique rules on emails and category names.
    // Normally the services catch these first, but if two requests happen at the exact same time one can slip through.
    // It logs the database's reason for us and sends the user a 409 without any database details.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {

        log.warn("Database refused a change on {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT, "That clashes with data that already exists", request, null);

    }

    // When something isn't found, like an account id that doesn't exist or belongs to someone else, it sends back 404 Not found.
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {

        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);

    }

    // When the request clashes with what's already in the database, like an email that''s already taken, it sends back 409 Conflict.
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex, HttpServletRequest request) {

        return build(HttpStatus.CONFLICT, ex.getMessage(), request, null);

    }

    // This runs when @Valid fails on a request body so one of the rules in a request record got broken.
    // It goes through every field that failed and makes a list where each one is the field name followed by what went wrong with it.
    // That way the client sees every problem at once instead of fixing them one by one.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation failed", request, errors);

    }

    // This runs when the body can't be turned into the request object at all.
    // Like if the body is missing, the JSON is broken or an enum value got sent that doesn't exist.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {

        return build(HttpStatus.BAD_REQUEST, "Request body is missing or not valid JSON", request, null);

    }

    // This runs when something in the URL is the wrong type, like /api/accounts/abc when the id should be a number.
    // ex.getName() is the name of the bad parameter so the message says which one was wrong.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        return build(HttpStatus.BAD_REQUEST, "Invalid value for '" + ex.getName() + "'", request, null);

    }

    // This runs when login fails and sends back 401 Unauthorized.
    // The message is the same whether the email or the password was wrong so nobody can use it to check which emails have accounts
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {

        return build(HttpStatus.UNAUTHORIZED, "Invalid email or password", request, null);
    }

    // This runs when someone hits a URL that doesn't exist at all so they still get a 404 in our normal error format.
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResource(NoResourceFoundException ex, HttpServletRequest request) {

        return build(HttpStatus.NOT_FOUND, "Not found", request, null);

    }

    // This runs when the URL exists but with the wrong method, like sending a DELETE to an endpoint that only takes GET.
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        return build(HttpStatus.METHOD_NOT_ALLOWED, "Method " + ex.getMethod() + " is not allowed here", request, null);

    }

    // This catches anything the other handlers didn't so basically any bug we didn't see coming.
    // It logs the full error and stack trace so we can find the problem, the {} gets swapped for the URL.
    // The user only gets a basic message back so no internal details or stack traces leak out.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleEverythingElse(Exception ex, HttpServletRequest request) {

        log.error("Unexpected error on {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong", request, null);

    }

    // This runs when a required ?something= is left out of the URL, like calling a report without ?month=.
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {

        return build(HttpStatus.BAD_REQUEST, "Missing required parameter '" + ex.getParameterName() + "'", request, null);

    }

    // Every handler above uses this to make the actual response so they all come out in the same format.
    // It fills in the time, the status code, the message, the URL that was hit and the list of errors if there is one.
    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest request, List<String> errors) {

        ApiError body = new ApiError(Instant.now(), status.value(), message, request.getRequestURI(), errors);
        return ResponseEntity.status(status).body(body);

    }
}
