package com.codzilla.backend.User.DTO;

public record UserInfoResponseDTO(
        String nickname,
        String email,
        Integer rating,
        String iconUrl
) {
}
