package codzilla.backend.authservice.dto;

public record LoginRequestDTO(
        String email,
        String rawPassword
) {
}
