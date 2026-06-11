package com.codzilla.backend.Rating;

import java.util.UUID;

public record MatchFinishedEvent(UUID matchId, UUID winnerId, UUID loserId) {}
