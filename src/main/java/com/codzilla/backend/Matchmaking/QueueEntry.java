package com.codzilla.backend.Matchmaking;

import java.time.Instant;
import java.util.UUID;

public record QueueEntry(
        UUID userId,
        int rating,
        Instant joinedAt
) {
    public int ratingWindow() {
        long secondsWaiting = Instant.now().getEpochSecond() - joinedAt.getEpochSecond();
        int wave = (int) (secondsWaiting / 5) * MatchmakingService.WAVE_STEP;
        return Math.min(wave + MatchmakingService.BASE_WINDOW, MatchmakingService.MAX_WINDOW);
    }
}
