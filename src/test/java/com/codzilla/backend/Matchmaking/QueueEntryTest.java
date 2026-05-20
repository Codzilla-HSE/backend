package com.codzilla.backend.Matchmaking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class QueueEntryTest {

    private QueueEntry entryWaitingFor(long seconds) {
        return new QueueEntry(UUID.randomUUID(), 1000, Instant.now().minusSeconds(seconds));
    }

    @Test
    @DisplayName("ratingWindow: только что вошёл — базовое окно BASE_WINDOW")
    void ratingWindow_newEntry_returnsBaseWindow() {
        QueueEntry entry = entryWaitingFor(0);
        assertThat(entry.ratingWindow()).isEqualTo(MatchmakingService.BASE_WINDOW);
    }

    @Test
    @DisplayName("ratingWindow: ждёт 5 сек — окно BASE_WINDOW + WAVE_STEP")
    void ratingWindow_after5Seconds_grows() {
        QueueEntry entry = entryWaitingFor(5);
        int expected = MatchmakingService.BASE_WINDOW + MatchmakingService.WAVE_STEP;
        assertThat(entry.ratingWindow()).isEqualTo(expected);
    }

    @Test
    @DisplayName("ratingWindow: ждёт 10 сек — окно BASE_WINDOW + 2 * WAVE_STEP")
    void ratingWindow_after10Seconds_growsFurther() {
        QueueEntry entry = entryWaitingFor(10);
        int expected = MatchmakingService.BASE_WINDOW + 2 * MatchmakingService.WAVE_STEP;
        assertThat(entry.ratingWindow()).isEqualTo(expected);
    }

    @Test
    @DisplayName("ratingWindow: очень долгое ожидание — окно не превышает MAX_WINDOW")
    void ratingWindow_veryLongWait_cappedAtMaxWindow() {
        QueueEntry entry = entryWaitingFor(100_000);
        assertThat(entry.ratingWindow()).isEqualTo(MatchmakingService.MAX_WINDOW);
    }

    @Test
    @DisplayName("ratingWindow: окно растёт ступенчато — каждые 5 сек")
    void ratingWindow_growsInSteps() {
        assertThat(entryWaitingFor(4).ratingWindow())
                .isEqualTo(MatchmakingService.BASE_WINDOW);

        assertThat(entryWaitingFor(5).ratingWindow())
                .isEqualTo(MatchmakingService.BASE_WINDOW + MatchmakingService.WAVE_STEP);

        assertThat(entryWaitingFor(9).ratingWindow())
                .isEqualTo(MatchmakingService.BASE_WINDOW + MatchmakingService.WAVE_STEP);

        assertThat(entryWaitingFor(10).ratingWindow())
                .isEqualTo(MatchmakingService.BASE_WINDOW + 2 * MatchmakingService.WAVE_STEP);
    }
}