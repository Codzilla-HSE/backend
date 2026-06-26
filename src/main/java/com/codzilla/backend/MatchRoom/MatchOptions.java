package com.codzilla.backend.MatchRoom;


public record MatchOptions(
        String statement,
        String language,
        String problemType,
        String problemLevel
) {
}
