package io.github.brooswitminecraft.layeredslopedynamicpaths;

import io.github.brooswitminecraft.layeredslopes.Anchor;
import io.github.brooswitminecraft.layeredslopes.LayerMath;
import io.github.brooswitminecraft.layeredslopes.LayeredBlock;
import io.github.brooswitminecraft.layeredslopes.LayeredSlopesMod;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * MINECRAFT-512 acceptance evidence: real runtime proof, through a real NeoForge server with
 * this mod plus its Layered Slopes dependency actually loaded, that {@link LayerErosion} erodes
 * full/layered blocks correctly. Exercises the pure decision API directly (same precedent
 * {@code smooth-slopes}' own {@code CoinstalledGameTests} established for exercising a sibling
 * extraction's helpers directly) rather than simulating real mob physics, since the probability
 * and transition logic under test lives entirely in {@link LayerErosion}, not in entity movement.
 * {@code ci.yml}'s "Run GameTests" step runs {@code ./gradlew runGameTestServer}.
 */
@GameTestHolder(LayeredSlopeDynamicPathsMod.MODID)
@PrefixGameTestTemplate(false)
public final class DynamicPathsGameTests {
    private DynamicPathsGameTests() {}

    /**
     * A chance of exactly 1.0 always succeeds the roll ({@code random.nextDouble()} is in
     * [0, 1), so it is always strictly less than 1.0); a chance of exactly 0.0 always fails it
     * ({@code nextDouble() >= 0.0} is always true) -- no fake/seeded {@link RandomSource} is
     * needed for either boundary, a real one works deterministically at the edges.
     */
    private static RandomSource random(GameTestHelper helper) {
        return helper.getLevel().getRandom();
    }

    @GameTest(template = "flat_platform")
    public static void erodingAFullLayerableBlockConvertsItOneLayerDown(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Blocks.DIRT.defaultBlockState());
        BlockPos absolute = helper.absolutePos(pos);

        LayerErosion.Result result = LayerErosion.erode(helper.getLevel(), absolute, 1.0, random(helper), true);

        helper.assertTrue(result == LayerErosion.Result.CONVERTED, "expected CONVERTED, got " + result);
        BlockState after = helper.getLevel().getBlockState(absolute);
        helper.assertTrue(after.getBlock() == LayeredSlopesMod.LAYERED_DIRT.get(), "expected layered dirt, got " + after);
        helper.assertTrue(after.getValue(LayeredBlock.LAYERS) == LayerMath.MAX_LAYERS - 1,
                "expected " + (LayerMath.MAX_LAYERS - 1) + " layers, got " + after.getValue(LayeredBlock.LAYERS));
        helper.succeed();
    }

    @GameTest(template = "flat_platform")
    public static void erodingALayeredBlockRemovesExactlyOneLayer(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        int startLayers = 5;
        helper.setBlock(pos, LayeredSlopesMod.LAYERED_STONE.get().defaultBlockState()
                .setValue(LayeredBlock.LAYERS, startLayers).setValue(LayeredBlock.ANCHOR, Anchor.FLOOR));
        BlockPos absolute = helper.absolutePos(pos);

        LayerErosion.Result result = LayerErosion.erode(helper.getLevel(), absolute, 1.0, random(helper), true);

        helper.assertTrue(result == LayerErosion.Result.REMOVED, "expected REMOVED, got " + result);
        BlockState after = helper.getLevel().getBlockState(absolute);
        helper.assertTrue(after.getValue(LayeredBlock.LAYERS) == startLayers - 1,
                "expected " + (startLayers - 1) + " layers, got " + after.getValue(LayeredBlock.LAYERS));
        helper.succeed();
    }

    @GameTest(template = "flat_platform")
    public static void erodingTheLastLayerVanishesToAir(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, LayeredSlopesMod.LAYERED_STONE.get().defaultBlockState()
                .setValue(LayeredBlock.LAYERS, LayerMath.MIN_LAYERS).setValue(LayeredBlock.ANCHOR, Anchor.FLOOR));
        BlockPos absolute = helper.absolutePos(pos);

        LayerErosion.Result result = LayerErosion.erode(helper.getLevel(), absolute, 1.0, random(helper), true);

        helper.assertTrue(result == LayerErosion.Result.VANISHED, "expected VANISHED, got " + result);
        helper.assertTrue(helper.getLevel().getBlockState(absolute).isAir(), "expected air after the last layer is removed");
        helper.succeed();
    }

    @GameTest(template = "flat_platform")
    public static void erodingAFullGrassBlockDisturbsItStraightToLayeredDirt(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState());
        BlockPos absolute = helper.absolutePos(pos);

        LayerErosion.Result result = LayerErosion.erode(helper.getLevel(), absolute, 1.0, random(helper), true);

        helper.assertTrue(result == LayerErosion.Result.CONVERTED, "expected CONVERTED, got " + result);
        BlockState after = helper.getLevel().getBlockState(absolute);
        helper.assertTrue(after.getBlock() == LayeredSlopesMod.LAYERED_DIRT.get(),
                "footfall erosion must disturb grass straight to dirt, got " + after);
        helper.succeed();
    }

    @GameTest(template = "flat_platform")
    public static void erodingAnExistingLayeredGrassBlockSettlesToLayeredDirt(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        int startLayers = 8;
        helper.setBlock(pos, LayeredSlopesMod.LAYERED_GRASS.get().defaultBlockState()
                .setValue(LayeredBlock.LAYERS, startLayers).setValue(LayeredBlock.ANCHOR, Anchor.FLOOR));
        BlockPos absolute = helper.absolutePos(pos);

        LayerErosion.Result result = LayerErosion.erode(helper.getLevel(), absolute, 1.0, random(helper), true);

        helper.assertTrue(result == LayerErosion.Result.REMOVED, "expected REMOVED, got " + result);
        BlockState after = helper.getLevel().getBlockState(absolute);
        helper.assertTrue(after.getBlock() == LayeredSlopesMod.LAYERED_DIRT.get(),
                "disturbed layered grass must settle as layered dirt, got " + after);
        helper.assertTrue(after.getValue(LayeredBlock.LAYERS) == startLayers - 1,
                "expected " + (startLayers - 1) + " layers, got " + after.getValue(LayeredBlock.LAYERS));
        helper.succeed();
    }

    @GameTest(template = "flat_platform")
    public static void zeroChanceNeverErodes(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Blocks.DIRT.defaultBlockState());
        BlockPos absolute = helper.absolutePos(pos);

        LayerErosion.Result result = LayerErosion.erode(helper.getLevel(), absolute, 0.0, helper.getLevel().getRandom(), true);

        helper.assertTrue(result == LayerErosion.Result.RESISTED, "expected RESISTED, got " + result);
        helper.assertTrue(helper.getLevel().getBlockState(absolute).is(Blocks.DIRT), "a zero chance must never change the block");
        helper.succeed();
    }

    @GameTest(template = "flat_platform")
    public static void disabledErosionNeverErodesEvenWithACertainRoll(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Blocks.DIRT.defaultBlockState());
        BlockPos absolute = helper.absolutePos(pos);

        LayerErosion.Result result = LayerErosion.erode(helper.getLevel(), absolute, 1.0, random(helper), false);

        helper.assertTrue(result == LayerErosion.Result.DISABLED, "expected DISABLED, got " + result);
        helper.assertTrue(helper.getLevel().getBlockState(absolute).is(Blocks.DIRT), "a disabled erosion must never change the block");
        helper.succeed();
    }

    @GameTest(template = "flat_platform")
    public static void anUnrelatedBlockIsSkipped(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Blocks.OAK_PLANKS.defaultBlockState());
        BlockPos absolute = helper.absolutePos(pos);

        LayerErosion.Result result = LayerErosion.erode(helper.getLevel(), absolute, 1.0, random(helper), true);

        helper.assertTrue(result == LayerErosion.Result.SKIPPED, "expected SKIPPED, got " + result);
        helper.assertTrue(helper.getLevel().getBlockState(absolute).is(Blocks.OAK_PLANKS), "an unrelated block must never change");
        helper.succeed();
    }

    /**
     * Regression for the self-review finding (PR #1 comment 52901): a landing that happens to
     * fall on the same tick a walking-cadence sample would also erode must still erode at most
     * once. Exercises {@link FootfallDedup} against a real spawned entity, then replays the exact
     * {@code chance > 0 && FootfallDedup.claim(entity, tick)} guard both {@link WalkErosion} and
     * {@link LandingErosion} use, in registration order (landing first): the second source's
     * claim must fail and its erosion call must never happen, leaving only one layer removed.
     */
    @GameTest(template = "flat_platform")
    public static void walkingAndLandingOnTheSameTickErodeAtMostOnce(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        int startLayers = 5;
        helper.setBlock(pos, LayeredSlopesMod.LAYERED_STONE.get().defaultBlockState()
                .setValue(LayeredBlock.LAYERS, startLayers).setValue(LayeredBlock.ANCHOR, Anchor.FLOOR));
        BlockPos absolute = helper.absolutePos(pos);
        Pig entity = helper.spawn(EntityType.PIG, pos);
        long tick = 123L;

        // Landing's Post listener runs first: it claims the entity for this tick and erodes.
        helper.assertTrue(FootfallDedup.claim(entity, tick), "landing must win the race and claim first");
        LayerErosion.Result landingResult = LayerErosion.erode(helper.getLevel(), absolute, 1.0, random(helper), true);
        helper.assertTrue(landingResult == LayerErosion.Result.REMOVED, "expected REMOVED, got " + landingResult);

        // Walking's listener runs second on the SAME tick: its claim must fail, so its erosion
        // call must never happen at all (not merely resist the probability roll).
        helper.assertTrue(!FootfallDedup.claim(entity, tick),
                "walking must not also claim the same entity's contact on the same tick");

        BlockState after = helper.getLevel().getBlockState(absolute);
        helper.assertTrue(after.getValue(LayeredBlock.LAYERS) == startLayers - 1,
                "expected exactly one layer removed (not two) for one same-tick contact, got "
                        + (startLayers - after.getValue(LayeredBlock.LAYERS)) + " removed");
        helper.succeed();
    }

    /**
     * Regression for the self-review finding (PR #1 comment 52912): a never-before-seen entity
     * (e.g. one that just entered a player's scan radius mid-fall) must not have its very first
     * landing silently missed merely because it was the first tick it was ever observed. Drives
     * {@link LandingTracker} (the real decision machinery {@link LandingErosion} delegates to)
     * against a real spawned entity, exactly as {@code onPreTick}/{@code onPostTick} would for an
     * entity that is airborne the instant it is first observed and touches down this same tick,
     * then erodes through the real {@link LayerErosion} API and asserts exactly one layer removed.
     */
    @GameTest(template = "flat_platform")
    public static void aNewlyObservedAirborneEntityLandingErodesOnce(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        int startLayers = 5;
        helper.setBlock(pos, LayeredSlopesMod.LAYERED_STONE.get().defaultBlockState()
                .setValue(LayeredBlock.LAYERS, startLayers).setValue(LayeredBlock.ANCHOR, Anchor.FLOOR));
        BlockPos absolute = helper.absolutePos(pos);
        Pig entity = helper.spawn(EntityType.PIG, pos);

        // First-ever observation of this entity, already airborne (as LandingErosion.onPreTick
        // would report), then movement resolves to grounded within that same tick (onPostTick).
        LandingTracker.observePre(entity, false, 8.0);
        Double impactSpeed = LandingTracker.observePostAndConsumeImpact(entity, true);
        helper.assertTrue(impactSpeed != null, "a newly observed airborne entity's first landing must be detected");

        double chance = LandingMath.chance(1.0, impactSpeed, 1.0, 1.0, 1.0, 0.0, 1.0);
        LayerErosion.Result result = LayerErosion.erode(helper.getLevel(), absolute, chance, random(helper), true);

        helper.assertTrue(result == LayerErosion.Result.REMOVED, "expected REMOVED, got " + result);
        BlockState after = helper.getLevel().getBlockState(absolute);
        helper.assertTrue(after.getValue(LayeredBlock.LAYERS) == startLayers - 1,
                "expected exactly one layer removed for the first-observed landing, got "
                        + (startLayers - after.getValue(LayeredBlock.LAYERS)) + " removed");
        helper.succeed();
    }

    /**
     * Regression for a fallback GitHub review finding on PR #1 (head a015167): {@code
     * LandingErosion.onPreTick} passed a real entity's {@code getDeltaMovement().y} (blocks PER
     * TICK) straight into {@link LandingMath}, which expects m/s (= blocks per SECOND) exactly
     * like {@link WalkMath} already converts for walking speed; the previous tests only injected
     * already-converted numbers like {@code 8.0} directly into {@link LandingTracker}, bypassing
     * the real conversion entirely. Strengthened for MINECRAFT-515 (self-review): the previous
     * version of this test recomputed its own separate copy of the {@code velocity * 20}
     * expression instead of calling {@link LandingErosion#impactSpeedMetersPerSecond}, the actual
     * adapter {@code onPreTick} invokes -- deleting/breaking the conversion there would not have
     * failed this test at all. Drives a REAL spawned entity's actual {@code getDeltaMovement()}
     * through that exact production adapter and asserts the real Minecraft per-tick unit is
     * correctly scaled to m/s.
     */
    @GameTest(template = "flat_platform")
    public static void aRealEntitysFallSpeedConvertsFromBlocksPerTickToMetersPerSecond(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        Pig entity = helper.spawn(EntityType.PIG, pos);
        double impactMetersPerSecond = 8.0;
        entity.setDeltaMovement(new Vec3(0, -impactMetersPerSecond / LandingErosion.TICKS_PER_SECOND, 0));

        double observed = LandingErosion.impactSpeedMetersPerSecond(entity);

        helper.assertTrue(Math.abs(observed - impactMetersPerSecond) < 1e-9,
                "expected " + impactMetersPerSecond + " m/s after conversion, got " + observed);
        helper.succeed();
    }

    /**
     * Regression for a fallback GitHub review finding on PR #1 (head a015167): {@link
     * LayerErosion} mutations had no protection veto at all, letting mobs/players erode claimed
     * blocks. Mirrors the sibling Layered Slopes Flowing Fluids extraction's own equivalent test
     * (MINECRAFT-513): register a listener that cancels the real {@code BlockEvent.BreakEvent}
     * {@link Protection} posts, then assert the erosion call reports PROTECTED and the block is
     * actually unchanged.
     */
    @GameTest(template = "flat_platform")
    public static void aCancelledProtectionHookLeavesTheBlockUnchanged(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Blocks.DIRT.defaultBlockState());
        BlockPos absolute = helper.absolutePos(pos);

        Consumer<BlockEvent.BreakEvent> listener = event -> event.setCanceled(true);
        NeoForge.EVENT_BUS.addListener(listener);
        try {
            LayerErosion.Result result = LayerErosion.erode(helper.getLevel(), absolute, 1.0, random(helper), true);
            helper.assertTrue(result == LayerErosion.Result.PROTECTED, "expected PROTECTED, got " + result);
            helper.assertTrue(helper.getLevel().getBlockState(absolute).is(Blocks.DIRT),
                    "a cancelled protection hook must not change the block");
        } finally {
            NeoForge.EVENT_BUS.unregister(listener);
        }
        helper.succeed();
    }
}
