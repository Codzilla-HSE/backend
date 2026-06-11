package com.codzilla.backend.PreMatch.MatchRoom;

import com.codzilla.backend.Rating.MatchFinishedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchFinishTest {

    @Mock private MatchRepository matchRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    private MatchService buildService() {
        return new MatchService(
                matchRepository,
                null,
                null,
                null,
                null,
                eventPublisher
        );
    }

    private Match liveMatch(UUID id, UUID first, UUID second) {
        Match m = new Match(first, second);
        m.id = id;
        m.setStatus(Match.Status.LIVE);
        return m;
    }

    @Test
    void firstAccepted_publishesEventWithWinnerAndLoser() {
        MatchService service = buildService();
        UUID matchId = UUID.randomUUID();
        UUID winner = UUID.randomUUID();
        UUID loser = UUID.randomUUID();
        Match match = liveMatch(matchId, winner, loser);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

        boolean finished = service.finishMatch(matchId, winner);

        assertThat(finished).isTrue();
        assertThat(match.getStatus()).isEqualTo(Match.Status.FINISHED);
        assertThat(match.getWinnerId()).isEqualTo(winner);

        ArgumentCaptor<MatchFinishedEvent> captor = ArgumentCaptor.forClass(MatchFinishedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().winnerId()).isEqualTo(winner);
        assertThat(captor.getValue().loserId()).isEqualTo(loser);
    }

    @Test
    void secondAccepted_isIdempotent_noSecondEvent() {
        MatchService service = buildService();
        UUID matchId = UUID.randomUUID();
        UUID winner = UUID.randomUUID();
        UUID loser = UUID.randomUUID();
        Match match = liveMatch(matchId, winner, loser);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

        service.finishMatch(matchId, winner);
        boolean second = service.finishMatch(matchId, loser);

        assertThat(second).isFalse();
        verify(eventPublisher, times(1)).publishEvent(any(MatchFinishedEvent.class));
    }
}