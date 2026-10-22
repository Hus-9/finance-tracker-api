package com.husnain.financetracker.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds){
}

