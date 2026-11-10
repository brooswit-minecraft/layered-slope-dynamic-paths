package io.github.brooswitminecraft.layeredslopedynamicpaths;

import io.github.brooswitminecraft.layeredslopes.Anchor;
import io.github.brooswitminecraft.layeredslopes.LayerMath;
import io.github.brooswitminecraft.layeredslopes.LayeredBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * {@code erode(chance)}: a single probabilistic decision to remove one layer from the block at
 * {@code pos}, whether it is already a {@link LayeredBlock} or a full "layerable" block
 * ({@link PathMaterials}). Callers (walking and landing) only report a bounded chance for the
 * contact they observed; this class alone decides whether the block actually changes. No
 * material is moved to a neighbour (unlike Dynamic Terrain's own erosion, which this mod does
 * not depend on): a removed layer simply disappears, which is footfall wear rather than a
 * smoothing/grading move.
 */
public final class LayerErosion {
    private LayerErosion() {}

    public enum Result {
        /** World erosion is disabled, or this is being evaluated client-side. */
        DISABLED,
        /** The target chunk is not loaded; never force-loads a chunk to erode it. */
        UNLOADED,
        /** The block at {@code pos} takes no part in footfall erosion (not layered or layerable). */
        SKIPPED,
        /** The probability roll failed; nothing changed. */
        RESISTED,
        /** A full layerable block converted into its layered variant, one layer down. */
        CONVERTED,
        /** A layered block lost one layer and still has at least one left. */
        REMOVED,
        /** A layered block was at its last layer; the block became air. */
        VANISHED,
    }

    public static Result erode(Level level, BlockPos pos, double chance, RandomSource random, boolean enabled) {
        if (level.isClientSide() || !enabled) {
            return Result.DISABLED;
        }
        if (!level.isLoaded(pos)) {
            return Result.UNLOADED;
        }
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof LayeredBlock existing) {
            return eat(level, pos, state, existing, chance, random);
        }
        LayeredBlock converted = PathMaterials.layeredFor(state.getBlock());
        if (converted == null) {
            return Result.SKIPPED;
        }
        if (random.nextDouble() >= chance) {
            return Result.RESISTED;
        }
        level.setBlock(pos, converted.defaultBlockState()
                .setValue(LayeredBlock.LAYERS, LayerMath.MAX_LAYERS - 1)
                .setValue(LayeredBlock.ANCHOR, Anchor.FLOOR), Block.UPDATE_ALL);
        return Result.CONVERTED;
    }

    private static Result eat(Level level, BlockPos pos, BlockState state, LayeredBlock block,
            double chance, RandomSource random) {
        if (random.nextDouble() >= chance) {
            return Result.RESISTED;
        }
        int layers = state.getValue(LayeredBlock.LAYERS);
        if (layers <= LayerMath.MIN_LAYERS) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            return Result.VANISHED;
        }
        LayeredBlock disturbed = PathMaterials.disturbedFrom(block);
        LayeredBlock target = disturbed != null ? disturbed : block;
        level.setBlock(pos, target.defaultBlockState()
                .setValue(LayeredBlock.LAYERS, layers - 1)
                .setValue(LayeredBlock.ANCHOR, state.getValue(LayeredBlock.ANCHOR)),
                Block.UPDATE_ALL);
        return Result.REMOVED;
    }
}
