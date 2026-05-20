package com.codzilla.backend.Matchmaking.dto;

public record MatchStatusDTO(
        String status,
        int queueSize,
        long waitingSeconds
) {}
