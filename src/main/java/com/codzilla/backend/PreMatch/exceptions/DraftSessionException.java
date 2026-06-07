package com.codzilla.backend.PreMatch.exceptions;

public class DraftSessionException extends RuntimeException {
    public enum DraftErrorType {
        NOT_YOUR_TURN,
        OPTION_DO_NOT_EXISTS,
        CAN_NOT_BAN_LAST_OPTION,
        CATEGORY_NOT_EXISTS_IN_SESSION, CATEGORY_IS_FINISHED, OPTION_ALREADY_BANNED
    }
    public DraftErrorType type;
    public DraftSessionException(DraftErrorType type) {
        this.type = type;
    }
}
