package com.codzilla.backend.PreMatch.exceptions;


public class MatchException extends RuntimeException {
    public enum ErrorType {

    }
    public ErrorType type;
    public MatchException(ErrorType type) {
        this.type = type;
    }
}
