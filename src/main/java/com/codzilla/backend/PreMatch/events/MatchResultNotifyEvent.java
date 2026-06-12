package com.codzilla.backend.PreMatch.events;

import java.util.UUID;

public record MatchResultNotifyEvent(
        UUID matchId,
        UUID winnerId,
        UUID loserId,
        String winnerEmail,
        String loserEmail,
        int winnerNewRating,
        int winnerRatingDelta,
        int loserNewRating,
        int loserRatingDelta
) {}
