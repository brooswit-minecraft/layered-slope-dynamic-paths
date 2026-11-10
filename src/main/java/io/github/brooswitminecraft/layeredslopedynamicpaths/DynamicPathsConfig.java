package io.github.brooswitminecraft.layeredslopedynamicpaths;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Server config. Defaults off, mirroring Dynamic Terrain's own terrain-altering defaults, so existing worlds are safe. */
public final class DynamicPathsConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue EROSION_ENABLED;
    public static final ModConfigSpec.BooleanValue WALK_EROSION;
    public static final ModConfigSpec.BooleanValue LANDING_EROSION;

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
        SPEC = builder.build();
    }

    private DynamicPathsConfig() {}
}
