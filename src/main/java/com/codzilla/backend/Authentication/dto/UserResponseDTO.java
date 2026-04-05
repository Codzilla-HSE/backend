package com.codzilla.backend.Authentication.dto;

import java.util.List;
import java.util.UUID;

public record UserResponseDTO(
        String nickname,
        String email,
        UUID id,
        List<String> authorities
) {
}
