package com.codzilla.backend.auth.dto;

public record RegisterRequestDTO(
        String username,
        String email,
        String rawPassword
) {
}
