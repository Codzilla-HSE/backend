package codzilla.backend.authservice.dto;

public record RegisterRequestDTO(
        String username,
        String email,
        String rawPassword
) {
}
