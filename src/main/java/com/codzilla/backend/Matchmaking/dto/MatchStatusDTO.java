package com.codzilla.backend.Matchmaking.dto;

import com.codzilla.backend.Matchmaking.MatchStatus;

public record MatchStatusDTO(
        MatchStatus status,
        int queueSize,
        long waitingSeconds
) {}