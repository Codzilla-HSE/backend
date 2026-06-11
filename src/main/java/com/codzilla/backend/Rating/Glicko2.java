package com.codzilla.backend.Rating;

import java.util.List;

public final class Glicko2 {

    private final double tau;

    private static final double EPSILON = 1e-6;

    private static final double SCALE = 173.7178;

    public Glicko2(double tau) {
        this.tau = tau;
    }

    public Glicko2() {
        this(0.5);
    }

    public record Opponent(double rating, double rd, double score) {}

    public record Result(double rating, double rd, double volatility) {}

    public Result update(double rating, double rd, double volatility, List<Opponent> games) {
        double mu = (rating - 1500.0) / SCALE;
        double phi = rd / SCALE;
        double sigma = volatility;

        if (games.isEmpty()) {
            double phiStar = Math.sqrt(phi * phi + sigma * sigma);
            return new Result(1500.0 + SCALE * mu, SCALE * phiStar, sigma);
        }

        double vInv = 0.0;
        double deltaSum = 0.0;
        for (Opponent op : games) {
            double muJ = (op.rating() - 1500.0) / SCALE;
            double phiJ = op.rd() / SCALE;
            double gPhiJ = g(phiJ);
            double eVal = e(mu, muJ, phiJ);
            vInv += gPhiJ * gPhiJ * eVal * (1.0 - eVal);
            deltaSum += gPhiJ * (op.score() - eVal);
        }
        double v = 1.0 / vInv;
        double delta = v * deltaSum;

        double newSigma = computeVolatility(phi, v, delta, sigma);

        double phiStar = Math.sqrt(phi * phi + newSigma * newSigma);

        double newPhi = 1.0 / Math.sqrt(1.0 / (phiStar * phiStar) + 1.0 / v);
        double newMu = mu + newPhi * newPhi * deltaSum;

        return new Result(1500.0 + SCALE * newMu, SCALE * newPhi, newSigma);
    }

    private double g(double phi) {
        return 1.0 / Math.sqrt(1.0 + 3.0 * phi * phi / (Math.PI * Math.PI));
    }

    private double e(double mu, double muJ, double phiJ) {
        return 1.0 / (1.0 + Math.exp(-g(phiJ) * (mu - muJ)));
    }

    private double computeVolatility(double phi, double v, double delta, double sigma) {
        double a = Math.log(sigma * sigma);
        double delta2 = delta * delta;
        double phi2 = phi * phi;

        java.util.function.DoubleUnaryOperator f = x ->
                Math.exp(x) * (delta2 - phi2 - v - Math.exp(x))
                        / (2.0 * Math.pow(phi2 + v + Math.exp(x), 2))
                        - (x - a) / (tau * tau);

        double A = a;
        double B;
        if (delta2 > phi2 + v) {
            B = Math.log(delta2 - phi2 - v);
        } else {
            double k = 1.0;
            while (f.applyAsDouble(a - k * tau) < 0.0) {
                k += 1.0;
            }
            B = a - k * tau;
        }

        double fA = f.applyAsDouble(A);
        double fB = f.applyAsDouble(B);

        while (Math.abs(B - A) > EPSILON) {
            double C = A + (A - B) * fA / (fB - fA);
            double fC = f.applyAsDouble(C);
            if (fC * fB <= 0.0) {
                A = B;
                fA = fB;
            } else {
                fA = fA / 2.0;
            }
            B = C;
            fB = fC;
        }
        return Math.exp(A / 2.0);
    }
}
