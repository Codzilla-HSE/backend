package com.codzilla.backend.Leaderboard.dto;

public record LeaderboardEntryDTO(
        int rank,
        String nickname,
        int rating,
        String avatarUrl,
        boolean isCurrentUser
) {}