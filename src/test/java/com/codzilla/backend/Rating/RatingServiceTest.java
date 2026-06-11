package com.codzilla.backend.Rating;

import com.codzilla.backend.User.User;
import com.codzilla.backend.User.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private UserRepository userRepository;

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
        RatingService service = new RatingService(userRepository);

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
    void unknownUser_skipsWithoutSaving() {
        RatingService service = new RatingService(userRepository);
        UUID winnerId = UUID.randomUUID();
        UUID loserId = UUID.randomUUID();

        when(userRepository.findById(winnerId)).thenReturn(Optional.empty());
        when(userRepository.findById(loserId)).thenReturn(Optional.of(newUser(loserId, 1500)));

        service.onMatchFinished(new MatchFinishedEvent(UUID.randomUUID(), winnerId, loserId));

        verify(userRepository, never()).save(any());
    }
}