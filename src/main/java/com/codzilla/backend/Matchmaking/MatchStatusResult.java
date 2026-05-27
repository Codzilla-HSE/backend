package com.codzilla.backend.Matchmaking;

public record MatchStatusResult(
        MatchStatus status,
        int queueSize,
        long waitingSeconds
) {}