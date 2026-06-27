package com.codzilla.backend.MatchRoom;

import com.codzilla.backend.MatchRoom.dto.MatchHistoryEntryDTO;
import com.codzilla.backend.User.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/match")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @GetMapping("/history")
    public ResponseEntity<List<MatchHistoryEntryDTO>> getMatchHistory(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(matchService.getMatchHistory(user.getId()));
    }
}
