package com.codzilla.backend.Authentication.dto;

import com.codzilla.backend.User.User;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record UserResponseDTO(
        String nickname,
        String email,
        UUID id,
        List<String> authorities
) {
    public UserResponseDTO(User user) {
        this(
                user.getNickname(),
                user.getEmail(),
                user.getId(),
                user.getAuthorities().stream().map(Objects::toString).toList()
        );
    }
}
