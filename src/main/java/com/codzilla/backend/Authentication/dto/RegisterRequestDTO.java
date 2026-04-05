package com.codzilla.backend.Authentication.dto;

public record RegisterRequestDTO(
        String nickname,
        String email,
        String rawPassword
) {
}
