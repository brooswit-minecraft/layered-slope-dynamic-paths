package io.github.brooswitminecraft.layeredslopedynamicpaths;

/**
 * Pure mapping from a landing entity's mass and pre-contact impact speed to a layer-removal
 * probability, given a server-configured rate ({@link DynamicPathsConfig#LANDING_BASE_CHANCE}
 * and friends). Bounded in [0, maxChance] and monotonic non-decreasing in impact speed for a
 * fixed mass. Independently tunable from {@link WalkMath}: each model reads its own separate
 * config values, so changing one model's rate/reference/cap never changes the other's. A heavy
 * landing from height is a much bigger single event than an ordinary walking step, so this
 * model's shipped defaults use a lower reference speed (a typical short hop) and a higher base
 * chance than {@link WalkMath}'s -- but a server operator is free to retune either independently.
 */
public final class LandingMath {
    /** Defaults, read by {@link DynamicPathsConfig} as the shipped config defaults. */
    public static final double DEFAULT_BASE_CHANCE = 0.05;
    public static final double DEFAULT_REFERENCE_MASS_KG = 100.0;
    public static final double DEFAULT_MIN_IMPACT_SPEED = 2.0;
    public static final double DEFAULT_REFERENCE_IMPACT_SPEED = 6.0;
    public static final double DEFAULT_MAX_CHANCE = 0.9;

    private LandingMath() {}

    /**
     * @param baseChance chance for a {@code referenceMassKg} entity landing at {@code referenceImpactSpeed}
     * @param minImpactSpeed impact speed (m/s) below which a "landing" is not an impact at all
     * @return a chance in [0, maxChance], 0 below {@code minImpactSpeed} or for non-positive mass
     */
    public static double chance(double massKg, double impactSpeed,
            double baseChance, double referenceMassKg, double referenceImpactSpeed, double minImpactSpeed, double maxChance) {
        if (!(massKg > 0) || !(impactSpeed >= minImpactSpeed)) {
            return 0.0;
        }
        double factor = (massKg / referenceMassKg) * (impactSpeed / referenceImpactSpeed);
        return Math.min(maxChance, baseChance * factor);
    }

    /** Convenience overload reading every control from {@link DynamicPathsConfig}. */
    public static double chance(double massKg, double impactSpeed) {
        return chance(massKg, impactSpeed,
                DynamicPathsConfig.LANDING_BASE_CHANCE.get(), DynamicPathsConfig.LANDING_REFERENCE_MASS_KG.get(),
                DynamicPathsConfig.LANDING_REFERENCE_IMPACT_SPEED.get(), DynamicPathsConfig.LANDING_MIN_IMPACT_SPEED.get(),
                DynamicPathsConfig.LANDING_MAX_CHANCE.get());
    }
}
