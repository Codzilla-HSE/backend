package com.codzilla.backend.dto;

public record LoginRequestDTO(
        String email,
        String rawPassword
) {
}
