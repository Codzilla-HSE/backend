package com.codzilla.backend.Matchmaking;

import com.codzilla.backend.User.User;
import com.codzilla.backend.User.UserRepository;
import com.codzilla.backend.PreMatch.MatchRoom.MatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchmakingServiceTest {

    @Mock
    private UserRepository userRepository;
    private MatchService matchService;

    private MatchmakingService service;

    private ConcurrentHashMap<UUID, QueueEntry> queue;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        service = new MatchmakingService(userRepository, matchService);

        Field queueField = MatchmakingService.class.getDeclaredField("queue");
        queueField.setAccessible(true);
        queue = (ConcurrentHashMap<UUID, QueueEntry>) queueField.get(service);
    }

    private QueueEntry entry(UUID userId, int rating, long waitingSeconds) {
        return new QueueEntry(userId, rating, Instant.now().minusSeconds(waitingSeconds));
    }

    private UUID addToQueue(int rating, long waitingSeconds) {
        UUID id = UUID.randomUUID();
        queue.put(id, entry(id, rating, waitingSeconds));
        return id;
    }

    private UUID addToQueue(int rating) {
        return addToQueue(rating, 0);
    }

    private UUID mockUser(int rating) {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id).rating(rating).email(id + "@test.com").build();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        return id;
    }

    @Test
    @DisplayName("enterQueue: игрок добавляется в очередь с правильным рейтингом")
    void enterQueue_addsUserWithCorrectRating() {
        UUID userId = mockUser(1200);

        service.enterQueue(userId);

        assertThat(queue).containsKey(userId);
        assertThat(queue.get(userId).rating()).isEqualTo(1200);
    }

    @Test
    @DisplayName("enterQueue: повторный вызов обновляет запись (рейтинг мог измениться)")
    void enterQueue_updatesExistingEntry() {
        UUID userId = mockUser(1200);
        service.enterQueue(userId);

        User updated = User.builder().id(userId).rating(1300).email(userId + "@test.com").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(updated));
        service.enterQueue(userId);

        assertThat(queue.get(userId).rating()).isEqualTo(1300);
    }

    @Test
    @DisplayName("leaveQueue: игрок убирается из очереди")
    void leaveQueue_removesUser() {
        UUID userId = addToQueue(1000);

        service.leaveQueue(userId);

        assertThat(queue).doesNotContainKey(userId);
    }

    @Test
    @DisplayName("leaveQueue: вызов для игрока не в очереди не бросает исключение")
    void leaveQueue_nonExistentUser_doesNotThrow() {
        assertThatNoException().isThrownBy(() -> service.leaveQueue(UUID.randomUUID()));
    }

    @Test
    @DisplayName("runMatchmaking: игроки с разницей рейтинга больше окна НЕ спариваются")
    void runMatchmaking_doesNotPairPlayersOutsideWindow() {
        UUID a = addToQueue(1000);
        UUID b = addToQueue(1100);

        service.runMatchmaking();

        assertThat(queue).containsKey(a).containsKey(b);
    }

    @Test
    @DisplayName("runMatchmaking: меньше двух игроков — ничего не происходит")
    void runMatchmaking_lessThanTwoPlayers_doesNothing() {
        addToQueue(1000);

        service.runMatchmaking();

        assertThat(queue).hasSize(1);
    }

    @Test
    @DisplayName("runMatchmaking: окно не превышает MAX_WINDOW даже при долгом ожидании")
    void runMatchmaking_windowCappedAtMaxWindow() {
        UUID a = addToQueue(1000, 10000);
        UUID b = addToQueue(1500);

        service.runMatchmaking();

        assertThat(queue).containsKey(a).containsKey(b);
    }

    @Test
    @DisplayName("queueStatus: игрок в очереди — статус WAITING")
    void queueStatus_playerInQueue_returnsWaiting() {
        UUID userId = addToQueue(1000, 5);

        var status = service.queueStatus(userId);

        assertThat(status.status()).isEqualTo(MatchStatus.WAITING);
        assertThat(status.queueSize()).isEqualTo(1);
        assertThat(status.waitingSeconds()).isGreaterThanOrEqualTo(5);
    }

    @Test
    @DisplayName("queueStatus: игрок не в очереди — статус NOT_IN_QUEUE")
    void queueStatus_playerNotInQueue_returnsNotInQueue() {
        var status = service.queueStatus(UUID.randomUUID());

        assertThat(status.status()).isEqualTo(MatchStatus.NOT_IN_QUEUE);
    }
}