package com.codzilla.backend.Authentication.dto;

import com.codzilla.backend.Authentication.Exceptions.RestException;
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
