package com.husnain.financetracker.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

// This is the shape of every error the API sends back so the client always gets errors in the same format whatever went wrong.
// @JsonInclude NON_NULL means any field that's null just gets left out of the JSON, instead of showing up as null.
// So for a normal error the errors list won't show at all, it only shows when there's actually something in it.

@JsonInclude(JsonInclude.Include.NON_NULL)

// timestamp is when the error happened.
// status is the HTTP status code, like 400 or 404.
// message is a short explanation of what went wrong.
// path is the URL  the request was sent to.
// errors is a list of the specific problems, like each field that failed validation.

public record ApiError(Instant timestamp, int status, String message, String path, List<String> errors) {
}
