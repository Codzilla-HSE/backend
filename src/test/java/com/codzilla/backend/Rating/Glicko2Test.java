package com.codzilla.backend.Rating;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Glicko2Test {

    @Test
    void matchesGlickmanReferenceExample() {
        Glicko2 g = new Glicko2(0.5);

        var games = List.of(
                new Glicko2.Opponent(1400, 30, 1.0),
                new Glicko2.Opponent(1550, 100, 0.0),
                new Glicko2.Opponent(1700, 300, 0.0)
        );

        Glicko2.Result r = g.update(1500, 200, 0.06, games);

        assertEquals(1464.06, r.rating(), 0.1);
        assertEquals(151.52, r.rd(), 0.1);
        assertEquals(0.05999, r.volatility(), 0.0001);
    }

    @Test
    void noGamesOnlyInflatesRd() {
        Glicko2 g = new Glicko2(0.5);
        Glicko2.Result r = g.update(1500, 200, 0.06, List.of());

        assertEquals(1500.0, r.rating(), 1e-9);
        assertEquals(0.06, r.volatility(), 1e-9);
        assertEquals(true, r.rd() > 200.0);
    }
}
