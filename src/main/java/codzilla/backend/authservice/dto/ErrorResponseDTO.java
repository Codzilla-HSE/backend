package codzilla.backend.authservice.dto;

import codzilla.backend.authservice.Exceptions.RestException;
import org.springframework.http.HttpStatus;

public record ErrorResponseDTO(
        String message,
        HttpStatus status
) {
    public ErrorResponseDTO(RestException exception) {
        this(
                exception.getMessage(),
                exception.getStatus()
        );
    }
}
