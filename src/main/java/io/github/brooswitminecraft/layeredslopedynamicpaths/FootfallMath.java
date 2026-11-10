package io.github.brooswitminecraft.layeredslopedynamicpaths;

/**
 * Pure mapping from an entity's bounding box to a rough mass, shared by both
 * the walking and landing probability models (MINECRAFT-512). Mirrors
 * Dynamic Terrain's own {@code EntityMath.massKg}; extracted rather than
 * reinvented since this mod has no Dynamic Terrain runtime dependency.
 */
public final class FootfallMath {
    /** Density used to turn a bounding box volume (m^3) into a rough mass. */
    public static final double DENSITY_KG_PER_M3 = 100.0;

    private FootfallMath() {}

    public static double massKg(double width, double height) {
        return Math.max(0.0, width * width * height * DENSITY_KG_PER_M3);
    }
}
