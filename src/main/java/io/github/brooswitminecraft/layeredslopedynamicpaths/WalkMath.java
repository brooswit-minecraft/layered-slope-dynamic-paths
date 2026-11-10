package io.github.brooswitminecraft.layeredslopedynamicpaths;

/**
 * Pure mapping from a walking entity's mass and ground speed to a
 * layer-removal probability. Bounded in [0, MAX_CHANCE] and monotonic
 * non-decreasing in speed for a fixed mass: {@code chance(m, s1) <=
 * chance(m, s2)} whenever {@code s1 <= s2}. Independently tunable from
 * {@link LandingMath}. Starting numbers, to be tuned in play.
 */
public final class WalkMath {
    /** Chance for a 100 kg entity moving at the reference speed. */
    public static final double BASE_CHANCE = 0.01;
    public static final double REFERENCE_MASS_KG = 100.0;
    public static final double REFERENCE_SPEED = 4.0;
    /** Below this ground speed (m/s) an entity is standing, not wearing the ground. */
    public static final double MIN_SPEED = 0.5;
    public static final double MAX_CHANCE = 0.25;

    private WalkMath() {}

    /** @return a chance in [0, MAX_CHANCE], 0 below {@link #MIN_SPEED} or for non-positive mass. */
    public static double chance(double massKg, double speed) {
        if (!(massKg > 0) || !(speed >= MIN_SPEED)) {
            return 0.0;
        }
        double factor = (massKg / REFERENCE_MASS_KG) * (speed / REFERENCE_SPEED);
        return Math.min(MAX_CHANCE, BASE_CHANCE * factor);
    }
}
