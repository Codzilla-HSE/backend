package com.codzilla.backend.Rating;

import com.codzilla.backend.User.User;
import com.codzilla.backend.User.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingService {

    private final UserRepository userRepository;
    private final Glicko2 glicko2 = new Glicko2();

    @EventListener
    @Transactional
    public void onMatchFinished(MatchFinishedEvent event) {
        recalculate(event.winnerId(), event.loserId());
    }

    public void recalculate(UUID winnerId, UUID loserId) {
        User winner = userRepository.findById(winnerId).orElse(null);
        User loser = userRepository.findById(loserId).orElse(null);
        if (winner == null || loser == null) {
            log.warn("Skip rating update, user not found: winner={}, loser={}", winnerId, loserId);
            return;
        }

        Glicko2.Opponent loserAsOpponentOfWinner =
                new Glicko2.Opponent(loser.getRating().doubleValue(),  loser.getRatingDeviation(),  1.0);
        Glicko2.Opponent winnerAsOpponentOfLoser =
                new Glicko2.Opponent(winner.getRating().doubleValue(), winner.getRatingDeviation(), 0.0);

        Glicko2.Result newWinner = glicko2.update(
                winner.getRating().doubleValue(), winner.getRatingDeviation(), winner.getVolatility(),
                List.of(loserAsOpponentOfWinner));
        Glicko2.Result newLoser = glicko2.update(
                loser.getRating().doubleValue(), loser.getRatingDeviation(), loser.getVolatility(),
                List.of(winnerAsOpponentOfLoser));

        Instant now = Instant.now();
        applyResult(winner, newWinner, now);
        applyResult(loser, newLoser, now);

        userRepository.save(winner);
        userRepository.save(loser);

        log.info("Rating updated: winner {} -> {}, loser {} -> {}",
                winnerId, winner.getRating(), loserId, loser.getRating());
    }

    private void applyResult(User user, Glicko2.Result r, Instant now) {
        user.setRating((int) Math.round(r.rating()));
        user.setRatingDeviation(r.rd());
        user.setVolatility(r.volatility());
        user.setLastMatchAt(now);
    }
}
