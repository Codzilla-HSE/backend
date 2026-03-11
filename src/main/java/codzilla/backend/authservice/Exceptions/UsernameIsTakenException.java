package codzilla.backend.authservice.Exceptions;

import org.springframework.http.HttpStatus;

public class UsernameIsTakenException extends RestException {
    public UsernameIsTakenException() {
        super(HttpStatus.CONFLICT, "Username is taken");
    }
}
