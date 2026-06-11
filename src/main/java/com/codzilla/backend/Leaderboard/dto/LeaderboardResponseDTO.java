package com.codzilla.backend.Leaderboard.dto;

import java.util.List;

public record LeaderboardResponseDTO(
        List<LeaderboardEntryDTO> top,
        List<LeaderboardEntryDTO> bottom,
        LeaderboardEntryDTO currentUser,
        int totalUsers
) {}