package com.codzilla.backend.Leaderboard;

import com.codzilla.backend.Leaderboard.dto.LeaderboardResponseDTO;
import com.codzilla.backend.User.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping
    public ResponseEntity<LeaderboardResponseDTO> getLeaderboard(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(leaderboardService.getLeaderboard(user.getEmail()));
    }
}