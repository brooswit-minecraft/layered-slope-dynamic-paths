package io.github.brooswitminecraft.layeredslopedynamicpaths;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import io.github.brooswitminecraft.layeredslopes.LayeredBlock;
import io.github.brooswitminecraft.layeredslopes.LayeredSlopesMod;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Pairs each vanilla full block that can be worn down by footfall ("layerable") with the
 * registered Layered Slopes variant it converts into on first erosion, plus the narrow
 * grass -> dirt settling map disturbance uses. Deliberately local and narrower than Dynamic
 * Terrain's own {@code LayeredMaterials} (which this mod must not depend on, per the
 * Dynamic-Paths-runs-without-Dynamic-Terrain boundary, and which MINECRAFT-511 is independently
 * relocating out of Dynamic Terrain into its own repo): no slab or group mappings, since those
 * only matter to manual grading, which stays out of scope here. Mirrors the base-block set
 * {@code smooth-slopes}' own {@code SmoothMaterials} already established for this same
 * extraction family, so nothing walkable before this split stops being erodible after it.
 */
final class PathMaterials {
    private static final Map<Block, Supplier<? extends LayeredBlock>> LAYERED_BY_BASE = new HashMap<>();
    private static final Map<Block, Supplier<? extends LayeredBlock>> DISTURBED_TO = new HashMap<>();

    static {
        register(Blocks.DIRT, LayeredSlopesMod.LAYERED_DIRT);
        register(Blocks.SAND, LayeredSlopesMod.LAYERED_SAND);
        register(Blocks.GRAVEL, LayeredSlopesMod.LAYERED_GRAVEL);
        register(Blocks.STONE, LayeredSlopesMod.LAYERED_STONE);
        register(Blocks.COBBLESTONE, LayeredSlopesMod.LAYERED_COBBLESTONE);
        register(Blocks.SMOOTH_STONE, LayeredSlopesMod.LAYERED_SMOOTH_STONE);
        register(Blocks.DEEPSLATE, LayeredSlopesMod.LAYERED_DEEPSLATE);
        register(Blocks.COBBLED_DEEPSLATE, LayeredSlopesMod.LAYERED_COBBLED_DEEPSLATE);
        register(Blocks.TERRACOTTA, LayeredSlopesMod.LAYERED_TERRACOTTA);
        register(Blocks.RED_TERRACOTTA, LayeredSlopesMod.LAYERED_RED_TERRACOTTA);
        register(Blocks.ORANGE_TERRACOTTA, LayeredSlopesMod.LAYERED_ORANGE_TERRACOTTA);
        register(Blocks.YELLOW_TERRACOTTA, LayeredSlopesMod.LAYERED_YELLOW_TERRACOTTA);
        register(Blocks.BROWN_TERRACOTTA, LayeredSlopesMod.LAYERED_BROWN_TERRACOTTA);
        register(Blocks.WHITE_TERRACOTTA, LayeredSlopesMod.LAYERED_WHITE_TERRACOTTA);
        register(Blocks.LIGHT_GRAY_TERRACOTTA, LayeredSlopesMod.LAYERED_LIGHT_GRAY_TERRACOTTA);
        register(Blocks.MAGENTA_TERRACOTTA, LayeredSlopesMod.LAYERED_MAGENTA_TERRACOTTA);
        register(Blocks.LIGHT_BLUE_TERRACOTTA, LayeredSlopesMod.LAYERED_LIGHT_BLUE_TERRACOTTA);
        register(Blocks.LIME_TERRACOTTA, LayeredSlopesMod.LAYERED_LIME_TERRACOTTA);
        register(Blocks.PINK_TERRACOTTA, LayeredSlopesMod.LAYERED_PINK_TERRACOTTA);
        register(Blocks.GRAY_TERRACOTTA, LayeredSlopesMod.LAYERED_GRAY_TERRACOTTA);
        register(Blocks.CYAN_TERRACOTTA, LayeredSlopesMod.LAYERED_CYAN_TERRACOTTA);
        register(Blocks.PURPLE_TERRACOTTA, LayeredSlopesMod.LAYERED_PURPLE_TERRACOTTA);
        register(Blocks.BLUE_TERRACOTTA, LayeredSlopesMod.LAYERED_BLUE_TERRACOTTA);
        register(Blocks.GREEN_TERRACOTTA, LayeredSlopesMod.LAYERED_GREEN_TERRACOTTA);
        register(Blocks.BLACK_TERRACOTTA, LayeredSlopesMod.LAYERED_BLACK_TERRACOTTA);
        register(Blocks.RED_SAND, LayeredSlopesMod.LAYERED_RED_SAND);
        register(Blocks.ICE, LayeredSlopesMod.LAYERED_ICE);
        register(Blocks.PACKED_ICE, LayeredSlopesMod.LAYERED_PACKED_ICE);
        register(Blocks.SNOW_BLOCK, LayeredSlopesMod.LAYERED_SNOW_BLOCK);
        register(Blocks.COARSE_DIRT, LayeredSlopesMod.LAYERED_COARSE_DIRT);
        register(Blocks.PODZOL, LayeredSlopesMod.LAYERED_PODZOL);
        register(Blocks.MYCELIUM, LayeredSlopesMod.LAYERED_MYCELIUM);
        register(Blocks.MUD, LayeredSlopesMod.LAYERED_MUD);
        register(Blocks.CLAY, LayeredSlopesMod.LAYERED_CLAY);
        register(Blocks.SANDSTONE, LayeredSlopesMod.LAYERED_SANDSTONE);
        register(Blocks.RED_SANDSTONE, LayeredSlopesMod.LAYERED_RED_SANDSTONE);
        register(Blocks.TUFF, LayeredSlopesMod.LAYERED_TUFF);
        register(Blocks.ANDESITE, LayeredSlopesMod.LAYERED_ANDESITE);
        register(Blocks.GRANITE, LayeredSlopesMod.LAYERED_GRANITE);
        register(Blocks.DIORITE, LayeredSlopesMod.LAYERED_DIORITE);
        register(Blocks.NETHERRACK, LayeredSlopesMod.LAYERED_NETHERRACK);
        register(Blocks.BLACKSTONE, LayeredSlopesMod.LAYERED_BLACKSTONE);
        // Footfall disturbs grass: the first erosion of a full grass block settles its top as
        // plain dirt rather than converting to layered grass, mirroring Dynamic Terrain's own
        // disturb=true erosion behavior (MINECRAFT-383) and its documented distinction from
        // manual grading (which preserves grass). This mod has no grading tool at all, so the
        // distinction here is simpler: footfall erosion always disturbs.
        register(Blocks.GRASS_BLOCK, LayeredSlopesMod.LAYERED_DIRT);
        register(Blocks.ROOTED_DIRT, LayeredSlopesMod.LAYERED_DIRT);

        // An already-layered grass block (placed directly, or grown by worldgen/Smooth Slopes)
        // settles the same way once footfall disturbs it.
        DISTURBED_TO.put(LayeredSlopesMod.LAYERED_GRASS.get(), LayeredSlopesMod.LAYERED_DIRT);
    }

    private PathMaterials() {}

    private static void register(Block base, Supplier<? extends LayeredBlock> layered) {
        LAYERED_BY_BASE.put(base, layered);
    }

    /** The layered variant a full block converts into on first erosion, or null if it has none. */
    static LayeredBlock layeredFor(Block base) {
        Supplier<? extends LayeredBlock> layered = LAYERED_BY_BASE.get(base);
        return layered == null ? null : layered.get();
    }

    /** What an already-layered block settles into once disturbed, or null if it is unaffected. */
    static LayeredBlock disturbedFrom(LayeredBlock block) {
        Supplier<? extends LayeredBlock> layered = DISTURBED_TO.get(block);
        return layered == null ? null : layered.get();
    }
}
