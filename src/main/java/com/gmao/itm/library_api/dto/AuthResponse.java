package com.gmao.itm.library_api.dto;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresInMs,
        String username,
        String role
) {}