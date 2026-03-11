package com.codzilla.backend.auth.dto;

import com.codzilla.backend.auth.Exceptions.RestException;
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
