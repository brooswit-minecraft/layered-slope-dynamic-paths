package io.github.brooswitminecraft.layeredslopedynamicpaths;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server config. Defaults off, mirroring Dynamic Terrain's own terrain-altering defaults, so
 * existing worlds are safe. Walking ({@code walk*}) and landing ({@code landing*}) probability
 * controls are deliberately separate config values read only by {@link WalkMath} respectively
 * {@link LandingMath}, so an operator can retune (or disable) either source without affecting
 * the other.
 */
public final class DynamicPathsConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue EROSION_ENABLED;
    public static final ModConfigSpec.BooleanValue WALK_EROSION;
    public static final ModConfigSpec.BooleanValue LANDING_EROSION;

    public static final ModConfigSpec.DoubleValue WALK_BASE_CHANCE;
    public static final ModConfigSpec.DoubleValue WALK_REFERENCE_MASS_KG;
    public static final ModConfigSpec.DoubleValue WALK_REFERENCE_SPEED;
    public static final ModConfigSpec.DoubleValue WALK_MIN_SPEED;
    public static final ModConfigSpec.DoubleValue WALK_MAX_CHANCE;

    public static final ModConfigSpec.DoubleValue LANDING_BASE_CHANCE;
    public static final ModConfigSpec.DoubleValue LANDING_REFERENCE_MASS_KG;
    public static final ModConfigSpec.DoubleValue LANDING_REFERENCE_IMPACT_SPEED;
    public static final ModConfigSpec.DoubleValue LANDING_MIN_IMPACT_SPEED;
    public static final ModConfigSpec.DoubleValue LANDING_MAX_CHANCE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        EROSION_ENABLED = builder
                .comment("Master switch for this mod. When false, neither walking nor landing erosion does anything.")
                .define("erosionEnabled", false);
        WALK_EROSION = builder
                .comment("Let walking players and mobs wear the ground under them (mass x speed). Needs erosionEnabled.")
                .define("walkErosion", true);
        LANDING_EROSION = builder
                .comment("Let landing players and mobs wear the ground they land on (mass x pre-contact impact speed). Needs erosionEnabled.")
                .define("landingErosion", true);

        builder.comment("Walking erosion probability model (WalkMath). Independent of landing* below.").push("walk");
        WALK_BASE_CHANCE = builder
                .comment("Chance for a walkReferenceMassKg entity moving at walkReferenceSpeed.")
                .defineInRange("walkBaseChance", WalkMath.DEFAULT_BASE_CHANCE, 0.0, 1.0);
        WALK_REFERENCE_MASS_KG = builder
                .comment("Reference mass (kg) walkBaseChance is calibrated against.")
                .defineInRange("walkReferenceMassKg", WalkMath.DEFAULT_REFERENCE_MASS_KG, 0.001, 100_000.0);
        WALK_REFERENCE_SPEED = builder
                .comment("Reference ground speed (m/s) walkBaseChance is calibrated against.")
                .defineInRange("walkReferenceSpeed", WalkMath.DEFAULT_REFERENCE_SPEED, 0.001, 1_000.0);
        WALK_MIN_SPEED = builder
                .comment("Below this ground speed (m/s) an entity is standing, not wearing the ground: chance is 0.")
                .defineInRange("walkMinSpeed", WalkMath.DEFAULT_MIN_SPEED, 0.0, 1_000.0);
        WALK_MAX_CHANCE = builder
                .comment("Hard cap on the walking chance, regardless of mass/speed.")
                .defineInRange("walkMaxChance", WalkMath.DEFAULT_MAX_CHANCE, 0.0, 1.0);
        builder.pop();

        builder.comment("Landing erosion probability model (LandingMath). Independent of walk* above.").push("landing");
        LANDING_BASE_CHANCE = builder
                .comment("Chance for a landingReferenceMassKg entity landing at landingReferenceImpactSpeed.")
                .defineInRange("landingBaseChance", LandingMath.DEFAULT_BASE_CHANCE, 0.0, 1.0);
        LANDING_REFERENCE_MASS_KG = builder
                .comment("Reference mass (kg) landingBaseChance is calibrated against.")
                .defineInRange("landingReferenceMassKg", LandingMath.DEFAULT_REFERENCE_MASS_KG, 0.001, 100_000.0);
        LANDING_REFERENCE_IMPACT_SPEED = builder
                .comment("Reference impact speed (m/s) landingBaseChance is calibrated against.")
                .defineInRange("landingReferenceImpactSpeed", LandingMath.DEFAULT_REFERENCE_IMPACT_SPEED, 0.001, 1_000.0);
        LANDING_MIN_IMPACT_SPEED = builder
                .comment("Impact speed (m/s) of a fall from 1 block, below which a \"landing\" is not an impact at all: chance is 0.")
                .defineInRange("landingMinImpactSpeed", LandingMath.DEFAULT_MIN_IMPACT_SPEED, 0.0, 1_000.0);
        LANDING_MAX_CHANCE = builder
                .comment("Hard cap on the landing chance, regardless of mass/impact speed.")
                .defineInRange("landingMaxChance", LandingMath.DEFAULT_MAX_CHANCE, 0.0, 1.0);
        builder.pop();

        SPEC = builder.build();
    }

    private DynamicPathsConfig() {}
}
