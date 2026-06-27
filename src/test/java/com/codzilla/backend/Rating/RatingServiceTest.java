package com.codzilla.backend.Rating;

import com.codzilla.backend.MatchRoom.MatchRepository;
import com.codzilla.backend.PreMatch.events.MatchResultNotifyEvent;
import com.codzilla.backend.User.User;
import com.codzilla.backend.User.UserRepository;
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
class RatingServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private User newUser(UUID id, int rating) {
        User u = User.builder()
                .email(id + "@test.com")
                .rating(rating)
                .ratingDeviation(350.0)
                .volatility(0.06)
                .build();
        u.setId(id);
        return u;
    }

    @Test
    void matchFinishedEvent_winnerGains_loserLoses() {
        RatingService service = new RatingService(userRepository, matchRepository, eventPublisher);

        UUID winnerId = UUID.randomUUID();
        UUID loserId = UUID.randomUUID();
        User winner = newUser(winnerId, 1500);
        User loser = newUser(loserId, 1500);

        when(userRepository.findById(winnerId)).thenReturn(Optional.of(winner));
        when(userRepository.findById(loserId)).thenReturn(Optional.of(loser));

        service.onMatchFinished(new MatchFinishedEvent(UUID.randomUUID(), winnerId, loserId));

        assertThat(winner.getRating()).isGreaterThan(1500);
        assertThat(loser.getRating()).isLessThan(1500);

        assertThat(winner.getRating()).isEqualTo(1662);
        assertThat(loser.getRating()).isEqualTo(1338);

        assertThat(winner.getRatingDeviation()).isLessThan(350.0);
        assertThat(loser.getRatingDeviation()).isLessThan(350.0);

        assertThat(winner.getLastMatchAt()).isNotNull();

        verify(userRepository).save(winner);
        verify(userRepository).save(loser);
    }

    @Test
    void matchFinishedEvent_publishesNotifyEventWithEmailsAndDeltas() {
        RatingService service = new RatingService(userRepository, matchRepository, eventPublisher);

        UUID winnerId = UUID.randomUUID();
        UUID loserId = UUID.randomUUID();
        User winner = newUser(winnerId, 1500);
        User loser = newUser(loserId, 1500);

        when(userRepository.findById(winnerId)).thenReturn(Optional.of(winner));
        when(userRepository.findById(loserId)).thenReturn(Optional.of(loser));

        service.onMatchFinished(new MatchFinishedEvent(UUID.randomUUID(), winnerId, loserId));

        ArgumentCaptor<MatchResultNotifyEvent> captor =
                ArgumentCaptor.forClass(MatchResultNotifyEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        MatchResultNotifyEvent published = captor.getValue();

        assertThat(published.winnerEmail()).isEqualTo(winner.getEmail());
        assertThat(published.loserEmail()).isEqualTo(loser.getEmail());

        assertThat(published.winnerNewRating()).isEqualTo(winner.getRating());
        assertThat(published.loserNewRating()).isEqualTo(loser.getRating());

        assertThat(published.winnerRatingDelta()).isEqualTo(162);
        assertThat(published.loserRatingDelta()).isEqualTo(162);
    }

    @Test
    void unknownUser_skipsWithoutSaving() {
        RatingService service = new RatingService(userRepository, matchRepository, eventPublisher);
        UUID winnerId = UUID.randomUUID();
        UUID loserId = UUID.randomUUID();

        when(userRepository.findById(winnerId)).thenReturn(Optional.empty());
        when(userRepository.findById(loserId)).thenReturn(Optional.of(newUser(loserId, 1500)));

        service.onMatchFinished(new MatchFinishedEvent(UUID.randomUUID(), winnerId, loserId));

        verify(userRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}