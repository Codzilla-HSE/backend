package com.codzilla.backend.auth.Exceptions;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class RestException extends RuntimeException {
    HttpStatus status;

    RestException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
