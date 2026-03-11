package com.codzilla.backend.Exceptions;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends RestException {
    public UserAlreadyExistsException() {
        super(HttpStatus.CONFLICT, "User already exists");
    }
}
