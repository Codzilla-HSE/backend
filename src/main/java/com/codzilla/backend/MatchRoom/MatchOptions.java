package com.codzilla.backend.MatchRoom;


public record MatchOptions(
        String title,
        String statement,
        String language,
        String problemType,
        String problemLevel
) {
}
