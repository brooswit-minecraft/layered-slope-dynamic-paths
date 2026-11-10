package io.github.brooswitminecraft.layeredslopedynamicpaths;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Walking entities as an erosion source. Every half second (cadence, dedup across entities and
 * ticks), living entities near a player that are on the ground and moving report mass x speed to
 * {@link LayerErosion#erode}. Mirrors Dynamic Terrain's own {@code EntityErosion}, which this mod
 * does not depend on; same bounded per-player radius scan to avoid unbounded per-tick work.
 */
public final class WalkErosion {
    private static final double RADIUS = 24.0;
    private static final int CADENCE_TICKS = 10;

    private WalkErosion() {}

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % CADENCE_TICKS != 0 || !DynamicPathsConfig.EROSION_ENABLED.get()
                || !DynamicPathsConfig.WALK_EROSION.get()) {
            return;
        }
        java.util.Set<LivingEntity> seen = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            AABB area = player.getBoundingBox().inflate(RADIUS);
            seen.addAll(player.serverLevel().getEntitiesOfClass(LivingEntity.class, area));
        }
        long tick = server.getTickCount();
        for (LivingEntity entity : seen) {
            if (!entity.onGround() || entity.isSpectator()) {
                continue;
            }
            double speed = Math.hypot(entity.getX() - entity.xo, entity.getZ() - entity.zo) * 20.0;
            double massKg = FootfallMath.massKg(entity.getBbWidth(), entity.getBbHeight());
            double chance = WalkMath.chance(massKg, speed);
            // Cross-source dedup (MINECRAFT-512 self-review): skip if a landing already claimed
            // this entity's contact for this same tick, so one footfall cannot erode twice.
            if (chance > 0 && FootfallDedup.claim(entity, tick)) {
                BlockPos under = entity.getOnPos();
                LayerErosion.erode(entity.level(), under, chance, entity.level().getRandom(), true);
            }
        }
    }
}
