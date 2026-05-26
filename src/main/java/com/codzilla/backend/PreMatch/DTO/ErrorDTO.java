package com.codzilla.backend.PreMatch.DTO;


public record ErrorDTO(
    ErrorStage stage,
    String message
) {
    public enum ErrorStage {
        DRAFT_ERROR,
        MATCH_ERROR
    }
}
