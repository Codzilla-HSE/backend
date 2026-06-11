package com.codzilla.backend.User.DTO;

import java.util.UUID;

public record UserInfoResponseDTO(
        String nickname,
        String email,
        Integer rating,
        String iconUrl,
        UUID id
) {
}
