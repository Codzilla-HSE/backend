package com.codzilla.backend.Matchmaking;

import com.codzilla.backend.Authentication.Exceptions.UserNotFoundException;
import com.codzilla.backend.User.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class MatchmakingService {

    public static final int BASE_WINDOW = 50;
    public static final int WAVE_STEP   = 25;
    public static final int MAX_WINDOW  = 400;

    private final ConcurrentHashMap<UUID, QueueEntry> queue = new ConcurrentHashMap<>();
    private final UserRepository userRepository;

    public MatchmakingService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void enterQueue(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        queue.put(userId, new QueueEntry(userId, user.getRating(), Instant.now()));
        log.info("[MM] User {} (rating={}) entered queue. Size: {}", userId, user.getRating(), queue.size());
    }

    public void leaveQueue(UUID userId) {
        queue.remove(userId);
        log.info("[MM] User {} left queue. Size: {}", userId, queue.size());
    }

    public MatchStatusResult queueStatus(UUID userId) {
        QueueEntry entry = queue.get(userId);
        if (entry == null) {
            return new MatchStatusResult(MatchStatus.NOT_IN_QUEUE, queue.size(), 0);
        }
        long waiting = Instant.now().getEpochSecond() - entry.joinedAt().getEpochSecond();
        return new MatchStatusResult(MatchStatus.WAITING, queue.size(), waiting);
    }

    public synchronized void runMatchmaking() {
        if (queue.size() < 2) return;

        List<QueueEntry> sorted = new ArrayList<>(queue.values());
        sorted.sort(Comparator.comparingInt(QueueEntry::rating));

        Set<UUID> matched = new HashSet<>();

        for (int i = 0; i < sorted.size() - 1; i++) {
            QueueEntry a = sorted.get(i);
            if (matched.contains(a.userId())) continue;

            for (int j = i + 1; j < sorted.size(); j++) {
                QueueEntry b = sorted.get(j);
                if (matched.contains(b.userId())) continue;

                int diff   = Math.abs(a.rating() - b.rating());
                int window = Math.min(a.ratingWindow(), b.ratingWindow());

                if (diff <= window) {
                    pair(a.userId(), b.userId());
                    matched.add(a.userId());
                    matched.add(b.userId());
                    break;
                }

                if (diff > MAX_WINDOW) break;
            }
        }
    }

    private void pair(UUID a, UUID b) {
        QueueEntry removedA = queue.remove(a);
        QueueEntry removedB = queue.remove(b);

        if (removedA == null || removedB == null) {
            if (removedA != null) queue.put(a, removedA);
            if (removedB != null) queue.put(b, removedB);
            log.warn("[MM] Pairing cancelled: one of users left queue ({} or {})", a, b);
            return;
        }
        // TODO implement create session call
        log.info("[MM] Paired: {} vs {}", a, b);
    }
}