package com.codzilla.backend.auth.Exceptions;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends RestException {
    public UserNotFoundException() {
        super(HttpStatus.CONFLICT, "User not found");
    }
}
