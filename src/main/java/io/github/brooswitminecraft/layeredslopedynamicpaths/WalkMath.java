package io.github.brooswitminecraft.layeredslopedynamicpaths;

/**
 * Pure mapping from a walking entity's mass and ground speed to a layer-removal probability,
 * given a server-configured rate ({@link DynamicPathsConfig#WALK_BASE_CHANCE} and friends).
 * Bounded in [0, maxChance] and monotonic non-decreasing in speed for a fixed mass: {@code
 * chance(m, s1, ...) <= chance(m, s2, ...)} whenever {@code s1 <= s2}. Independently tunable
 * from {@link LandingMath}: each model reads its own separate config values, so changing one
 * model's rate/reference/cap never changes the other's.
 */
public final class WalkMath {
    /** Defaults, read by {@link DynamicPathsConfig} as the shipped config defaults. */
    public static final double DEFAULT_BASE_CHANCE = 0.01;
    public static final double DEFAULT_REFERENCE_MASS_KG = 100.0;
    public static final double DEFAULT_REFERENCE_SPEED = 4.0;
    public static final double DEFAULT_MIN_SPEED = 0.5;
    public static final double DEFAULT_MAX_CHANCE = 0.25;

    private WalkMath() {}

    /**
     * @param baseChance chance for a {@code referenceMassKg} entity moving at {@code referenceSpeed}
     * @param minSpeed below this ground speed (m/s) an entity is standing, not wearing the ground
     * @return a chance in [0, maxChance], 0 below {@code minSpeed} or for non-positive mass
     */
    public static double chance(double massKg, double speed,
            double baseChance, double referenceMassKg, double referenceSpeed, double minSpeed, double maxChance) {
        if (!(massKg > 0) || !(speed >= minSpeed)) {
            return 0.0;
        }
        double factor = (massKg / referenceMassKg) * (speed / referenceSpeed);
        return Math.min(maxChance, baseChance * factor);
    }

    /** Convenience overload reading every control from {@link DynamicPathsConfig}. */
    public static double chance(double massKg, double speed) {
        return chance(massKg, speed,
                DynamicPathsConfig.WALK_BASE_CHANCE.get(), DynamicPathsConfig.WALK_REFERENCE_MASS_KG.get(),
                DynamicPathsConfig.WALK_REFERENCE_SPEED.get(), DynamicPathsConfig.WALK_MIN_SPEED.get(),
                DynamicPathsConfig.WALK_MAX_CHANCE.get());
    }
}
