package com.codzilla.backend.dto;

public record RegisterRequestDTO(
        String username,
        String email,
        String rawPassword
) {
}
