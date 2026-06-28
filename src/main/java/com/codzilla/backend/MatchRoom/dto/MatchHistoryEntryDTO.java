package com.codzilla.backend.MatchRoom.dto;

import java.time.Instant;
import java.util.UUID;

public record MatchHistoryEntryDTO(
        UUID matchId,
        String opponentNickname,
        boolean won,
        Integer rating,
        Instant finishedAt
) {}
