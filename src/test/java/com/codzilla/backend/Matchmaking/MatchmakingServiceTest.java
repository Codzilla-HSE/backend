package com.codzilla.backend.Matchmaking;

import com.codzilla.backend.User.User;
import com.codzilla.backend.User.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private MatchmakingService service;

    private ConcurrentHashMap<UUID, QueueEntry> queue;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        service = new MatchmakingService(userRepository, eventPublisher);

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
    @DisplayName("runMatchmaking: два игрока с близким рейтингом спариваются")
    void runMatchmaking_pairsPlayersWithCloseRating() {
        UUID a = addToQueue(1000);
        UUID b = addToQueue(1020);

        service.runMatchmaking();

        assertThat(queue).doesNotContainKey(a).doesNotContainKey(b);

        verify(eventPublisher, times(1)).publishEvent(any(MatchFoundEvent.class));
    }

    @Test
    @DisplayName("runMatchmaking: событие содержит правильные userId")
    void runMatchmaking_eventContainsCorrectUserIds() {
        UUID a = addToQueue(1000);
        UUID b = addToQueue(1030);

        service.runMatchmaking();

        ArgumentCaptor<MatchFoundEvent> captor = ArgumentCaptor.forClass(MatchFoundEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        MatchFoundEvent event = captor.getValue();
        assertThat(event.getPlayerOneId()).isIn(a, b);
        assertThat(event.getPlayerTwoId()).isIn(a, b);
        assertThat(event.getPlayerOneId()).isNotEqualTo(event.getPlayerTwoId());
    }

    @Test
    @DisplayName("runMatchmaking: игроки с разницей рейтинга больше окна НЕ спариваются")
    void runMatchmaking_doesNotPairPlayersOutsideWindow() {
        UUID a = addToQueue(1000);
        UUID b = addToQueue(1100);

        service.runMatchmaking();

        assertThat(queue).containsKey(a).containsKey(b);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("runMatchmaking: меньше двух игроков — ничего не происходит")
    void runMatchmaking_lessThanTwoPlayers_doesNothing() {
        addToQueue(1000);

        service.runMatchmaking();

        assertThat(queue).hasSize(1);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("runMatchmaking: пустая очередь — ничего не происходит")
    void runMatchmaking_emptyQueue_doesNothing() {
        service.runMatchmaking();

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("runMatchmaking: игрок ждёт 10 сек — окно расширяется до 100, пара находится")
    void runMatchmaking_expandingWindow_pairsAfterWaiting() {
        UUID a = addToQueue(1000, 10);
        UUID b = addToQueue(1090, 10);

        service.runMatchmaking();

        assertThat(queue).doesNotContainKey(a).doesNotContainKey(b);
        verify(eventPublisher, times(1)).publishEvent(any(MatchFoundEvent.class));
    }

    @Test
    @DisplayName("runMatchmaking: окно не превышает MAX_WINDOW даже при долгом ожидании")
    void runMatchmaking_windowCappedAtMaxWindow() {
        UUID a = addToQueue(1000, 10000);
        UUID b = addToQueue(1500);

        service.runMatchmaking();

        assertThat(queue).containsKey(a).containsKey(b);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("runMatchmaking: четыре игрока формируют две пары")
    void runMatchmaking_fourPlayers_twoMatches() {
        UUID a = addToQueue(1000);
        UUID b = addToQueue(1010);
        UUID c = addToQueue(1500);
        UUID d = addToQueue(1510);

        service.runMatchmaking();

        assertThat(queue).isEmpty();
        verify(eventPublisher, times(2)).publishEvent(any(MatchFoundEvent.class));
    }

    @Test
    @DisplayName("runMatchmaking: нечётное число игроков — один остаётся в очереди")
    void runMatchmaking_oddNumberOfPlayers_oneRemains() {
        addToQueue(1000);
        addToQueue(1010);
        addToQueue(1020);

        service.runMatchmaking();

        assertThat(queue).hasSize(1);
        verify(eventPublisher, times(1)).publishEvent(any(MatchFoundEvent.class));
    }

    @Test
    @DisplayName("runMatchmaking: если один игрок вышел из очереди — второй возвращается обратно")
    void runMatchmaking_onePlayerLeft_otherRequeued() {
        UUID a = addToQueue(1000);
        UUID b = addToQueue(1020);

        queue.remove(b);

        service.runMatchmaking();

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("queueStatus: игрок в очереди — статус WAITING")
    void queueStatus_playerInQueue_returnsWaiting() {
        UUID userId = addToQueue(1000, 5);

        var status = service.queueStatus(userId);

        assertThat(status.status()).isEqualTo("WAITING");
        assertThat(status.queueSize()).isEqualTo(1);
        assertThat(status.waitingSeconds()).isGreaterThanOrEqualTo(5);
    }

    @Test
    @DisplayName("queueStatus: игрок не в очереди — статус NOT_IN_QUEUE")
    void queueStatus_playerNotInQueue_returnsNotInQueue() {
        var status = service.queueStatus(UUID.randomUUID());

        assertThat(status.status()).isEqualTo("NOT_IN_QUEUE");
    }
}