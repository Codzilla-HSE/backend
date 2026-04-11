package com.codzilla.backend.Authentication.dto;

public record LoginRequestDTO(
        String email,
        String rawPassword
) {
}
