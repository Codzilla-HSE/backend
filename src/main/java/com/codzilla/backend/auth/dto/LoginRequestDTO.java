package com.codzilla.backend.auth.dto;

public record LoginRequestDTO(
        String email,
        String rawPassword
) {
}
