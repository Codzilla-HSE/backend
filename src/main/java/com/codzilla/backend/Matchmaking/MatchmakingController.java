package com.codzilla.backend.Matchmaking;

import com.codzilla.backend.Matchmaking.dto.MatchStatusDTO;
import com.codzilla.backend.User.User;
import com.codzilla.backend.User.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/matchmaking")
public class MatchmakingController {

    private final MatchmakingService matchmakingService;
    private final UserRepository userRepository;

    public MatchmakingController(MatchmakingService matchmakingService,
                                 UserRepository userRepository) {
        this.matchmakingService = matchmakingService;
        this.userRepository = userRepository;
    }

    private UUID resolveUserId(User principal) {
        return userRepository.findIdByEmail(principal.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @PostMapping("/queue")
    public ResponseEntity<Void> enterQueue(@AuthenticationPrincipal User user) {
        matchmakingService.enterQueue(resolveUserId(user));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/queue")
    public ResponseEntity<Void> leaveQueue(@AuthenticationPrincipal User user) {
        matchmakingService.leaveQueue(resolveUserId(user));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/queue/status")
    public ResponseEntity<MatchStatusDTO> queueStatus(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(matchmakingService.queueStatus(resolveUserId(user)));
    }
}