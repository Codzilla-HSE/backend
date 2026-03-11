package com.codzilla.backend.auth.dto;

import java.util.List;

public record UserResponseDTO(
        String username,
        String email,
        Long id,
        List<String> authorities
) {
}
