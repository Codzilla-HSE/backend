package com.codzilla.backend.auth.Exceptions;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends RestException {
    public UserAlreadyExistsException() {
        super(HttpStatus.CONFLICT, "User already exists");
    }
}
