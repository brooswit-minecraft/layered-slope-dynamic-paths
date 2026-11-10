package io.github.brooswitminecraft.layeredslopedynamicpaths;

/**
 * Pure mapping from a landing entity's mass and pre-contact impact speed to
 * a layer-removal probability. Bounded in [0, MAX_CHANCE] and monotonic
 * non-decreasing in impact speed for a fixed mass, independently tunable
 * from {@link WalkMath}: a heavy landing from height is a much bigger single
 * event than an ordinary walking step, so the reference speed is lower (a
 * typical short hop) and the base chance higher. Starting numbers, to be
 * tuned in play.
 */
public final class LandingMath {
    /** Chance for a 100 kg entity landing at the reference impact speed. */
    public static final double BASE_CHANCE = 0.05;
    public static final double REFERENCE_MASS_KG = 100.0;
    /** Impact speed (m/s) of a fall from 1 block, below which a "landing" is not an impact at all. */
    public static final double MIN_IMPACT_SPEED = 2.0;
    public static final double REFERENCE_IMPACT_SPEED = 6.0;
    public static final double MAX_CHANCE = 0.9;

    private LandingMath() {}

    /** @return a chance in [0, MAX_CHANCE], 0 below {@link #MIN_IMPACT_SPEED} or for non-positive mass. */
    public static double chance(double massKg, double impactSpeed) {
        if (!(massKg > 0) || !(impactSpeed >= MIN_IMPACT_SPEED)) {
            return 0.0;
        }
        double factor = (massKg / REFERENCE_MASS_KG) * (impactSpeed / REFERENCE_IMPACT_SPEED);
        return Math.min(MAX_CHANCE, BASE_CHANCE * factor);
    }
}
