package com.codzilla.backend;

import com.codzilla.backend.Exceptions.RestException;
import com.codzilla.backend.dto.ErrorResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.ObjectMapper;

@RestControllerAdvice
public class AuthExceptionHandler {
    @Autowired
    ObjectMapper objectMapper;

    @ExceptionHandler(RestException.class)
    public ResponseEntity<String> handleUserNotFound(RestException exception) {
        ErrorResponseDTO dto = new ErrorResponseDTO(exception);
        return new ResponseEntity<>(objectMapper.writeValueAsString(dto), exception.getStatus());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleWrongCredentials(BadCredentialsException exception){
        ErrorResponseDTO dto = new ErrorResponseDTO(
                "Wrong email or password",
                HttpStatus.UNAUTHORIZED
        );
        return new ResponseEntity<>(objectMapper.writeValueAsString(dto), HttpStatus.UNAUTHORIZED);
    }
}
