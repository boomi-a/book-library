package com.boomlibrary.dto.response;

public record AuthResponse(
        String token,
        String role,
        String username
) {}
