package io.github.brooswitminecraft.layeredslopedynamicpaths;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Best-effort respect for claim/protection mods (self-review finding on PR #1: {@code
 * LayerErosion} mutations had no protection veto, letting mobs/players erode claimed blocks).
 * This mod's erosion is an environmental system, not a player action, so there is no real
 * player break to intercept; mirrors the sibling Layered Slopes Flowing Fluids extraction's own
 * {@code Protection} (MINECRAFT-513, same org convention, itself following this org's existing
 * {@code TownDonationBoxGameTests} / dynamic-vehicles precedent for a cancellable, non-player
 * world change): post the same {@code BlockEvent.BreakEvent} a real break would, through a fake
 * player, and honor cancellation. This covers protection mods that listen on {@code BreakEvent}
 * (the common case); it is not a guarantee against every possible protection hook a mod might
 * use instead.
 */
final class Protection {
    private Protection() {}

    static boolean mayErode(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return true;
        }
        BlockState state = serverLevel.getBlockState(pos);
        var fakePlayer = FakePlayerFactory.getMinecraft(serverLevel);
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(serverLevel, pos, state, fakePlayer);
        NeoForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }
}
