package com.husnain.financetracker.dto;


// This is what the API sends back after a successful login.
// accessToken is the JWT itself, the client sends this with every request to prove who they are.
// tokenType says what kind of token it is, normally Bearer so the client knows to send it as Bearer then the token in the Authorization header.
// expiresInSeconds is how long the token lasts, after that the user has to log in again to get a new one.

public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds){
}

