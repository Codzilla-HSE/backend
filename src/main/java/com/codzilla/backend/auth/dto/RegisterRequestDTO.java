package com.codzilla.backend.auth.dto;

public record RegisterRequestDTO(
        String nickname,
        String email,
        String rawPassword
) {
}
