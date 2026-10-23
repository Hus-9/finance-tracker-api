package com.husnain.financetracker.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.husnain.financetracker.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

// This runs when someone tries to use an endpoint that needs them logged in, but they're not.
// So no token, a fake token or an expired one all end up here and get a 401 Unauthorized.
// We need this because Spring Security blocks the request before it reaches the controllers so GlobalExceptionHandler never sees it.
// Without this Spring would send back its own default error instead of our normal ApiError format.
// @Component makes it a Spring bean so SecurityConfig can use it.
@Component
@RequiredArgsConstructor

public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // ObjectMapper is what Spring uses to turn objects into JSON, we use the same one so the error looks exactly like our other errors.
    private final ObjectMapper objectMapper;

    // Spring Security calls this when it blocks a request that isn't logged in.
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

        // Makes the error body with a message telling them how to send their token.
        // The errors list is null because there's no field errors here so it just gets left out of the JSON.
        ApiError body = new ApiError(Instant.now(), HttpStatus.UNAUTHORIZED.value(), "You need to log in: send 'Authorization: Bearer <token>'", request.getRequestURI(), null);

        // We have to write the response ourselves here so it sets the status to 401, says it's JSON and then writes the ApiError as JSON into the response.
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);

    }
}
