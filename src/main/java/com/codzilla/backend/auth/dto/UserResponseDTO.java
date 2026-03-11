package com.codzilla.backend.auth.dto;

import java.util.List;

public record UserResponseDTO(
        String nickname,
        String email,
        Long id,
        List<String> authorities
) {
}
