package com.codzilla.backend.Matchmaking;

import com.codzilla.backend.Authentication.Exceptions.UserNotFoundException;
import com.codzilla.backend.User.UserRepository;
import com.codzilla.backend.PreMatch.MatchRoom.MatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class MatchmakingService {

    public static final int BASE_WINDOW = 50;
    public static final int WAVE_STEP   = 25;
    public static final int MAX_WINDOW  = 400;

    private static final long SSE_TIMEOUT = 10 * 60 * 1000L;

    private final ConcurrentHashMap<UUID, QueueEntry> queue = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final UserRepository userRepository;
    private final MatchService matchService;

    public MatchmakingService(UserRepository userRepository, MatchService matchService) {
        this.matchService = matchService;
        this.userRepository = userRepository;
    }

    public void enterQueue(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        queue.put(userId, new QueueEntry(userId, user.getRating(), Instant.now()));
        log.info("[MM] User {} (rating={}) entered queue. Size: {}", userId, user.getRating(), queue.size());
        broadcastQueueSize();
    }

    public void leaveQueue(UUID userId) {
        queue.remove(userId);
        log.info("[MM] User {} left queue. Size: {}", userId, queue.size());
        broadcastQueueSize();
    }

    public MatchStatusResult queueStatus(UUID userId) {
        QueueEntry entry = queue.get(userId);
        if (entry == null) {
            return new MatchStatusResult(MatchStatus.NOT_IN_QUEUE, queue.size(), 0);
        }
        long waiting = Instant.now().getEpochSecond() - entry.joinedAt().getEpochSecond();
        return new MatchStatusResult(MatchStatus.WAITING, queue.size(), waiting);
    }

    public SseEmitter subscribe(UUID userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> {
            emitters.remove(userId);
            emitter.complete();
        });

        emitter.onError((e) -> emitters.remove(userId));
        emitters.put(userId, emitter);

        sendToEmitter(emitter, "queue-size", String.valueOf(queue.size()));

        return emitter;
    }

    private void removeEmitter(UUID userId) {
        SseEmitter emitter = emitters.remove(userId);
        if (emitter != null) {
            emitter.complete();
        }
    }

    private void broadcastQueueSize() {
        String size = String.valueOf(queue.size());
        emitters.forEach((userId, emitter) ->
                sendToEmitter(emitter, "queue-size", size));
    }

    private void broadcastMatchFound(UUID userA, UUID userB, UUID sessionId) {
        String payload = sessionId.toString();
        for (UUID userId : List.of(userA, userB)) {
            SseEmitter emitter = emitters.remove(userId);
            if (emitter != null) {
                sendToEmitter(emitter, "match-found", payload);
                emitter.complete();
            }
        }
    }

    private void sendToEmitter(SseEmitter emitter, String eventName, String data) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
        } catch (IOException e) {
            log.warn("[MM] Failed to send SSE event '{}': {}", eventName, e.getMessage());
        }
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

        UUID userA = removedA.userId();
        UUID userB = removedB.userId();
        UUID sessionId = matchService.startMatch(userA, userB);
        broadcastQueueSize();
        broadcastMatchFound(userA, userB, sessionId);
        log.info("[MM] Paired: {} vs {} -> session {}", a, b, sessionId);
    }
}