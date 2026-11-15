package io.github.brooswitminecraft.layeredslopedynamicpaths;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Landing entities as an erosion source, distinct from walking. Captures the PRE-contact impact
 * velocity rather than the post-landing (already zeroed) velocity: {@link #onPreTick} reports
 * each nearby entity's ground state and current downward speed before this tick's movement/
 * collision runs, and {@link #onPostTick} reports the resulting ground state, to the shared pure
 * {@link LandingTracker} state machine, which decides whether a fresh landing happened and
 * returns its impact speed. See {@link LandingTracker}'s own doc for the self-review finding
 * this two-phase design fixes (a newly observed entity's first landing was previously missed).
 */
public final class LandingErosion {
    private static final double RADIUS = 24.0;
    /** Minecraft runs at 20 ticks/second; getDeltaMovement() is blocks/TICK, LandingMath expects m/s (= blocks/second). */
    static final double TICKS_PER_SECOND = 20.0;

    private LandingErosion() {}

    public static void onPreTick(ServerTickEvent.Pre event) {
        if (!DynamicPathsConfig.EROSION_ENABLED.get() || !DynamicPathsConfig.LANDING_EROSION.get()) {
            return;
        }
        forEachNearbyEntity(event.getServer(), entity ->
                LandingTracker.observePre(entity, entity.onGround(), impactSpeedMetersPerSecond(entity)));
    }

    /**
     * The actual production conversion seam: {@code entity.getDeltaMovement().y} is blocks/TICK
     * (negative while falling); {@link LandingMath} and {@link LandingTracker} expect a
     * non-negative downward speed in m/s (= blocks/second). This is the one place that
     * conversion happens -- {@link #onPreTick} calls it directly, and tests must call it too
     * (rather than recomputing the same expression) so that breaking the conversion here is
     * actually caught, instead of a test merely re-verifying its own separate copy of the formula.
     */
    static double impactSpeedMetersPerSecond(LivingEntity entity) {
        return -entity.getDeltaMovement().y * TICKS_PER_SECOND;
    }

    public static void onPostTick(ServerTickEvent.Post event) {
        if (!DynamicPathsConfig.EROSION_ENABLED.get() || !DynamicPathsConfig.LANDING_EROSION.get()) {
            return;
        }
        long tick = event.getServer().getTickCount();
        forEachNearbyEntity(event.getServer(), entity -> {
            Double impactSpeed = LandingTracker.observePostAndConsumeImpact(entity, entity.onGround());
            if (impactSpeed == null || entity.isSpectator()) {
                return;
            }
            double massKg = FootfallMath.massKg(entity.getBbWidth(), entity.getBbHeight());
            double chance = LandingMath.chance(massKg, impactSpeed);
            // Cross-source dedup (MINECRAFT-512 self-review): landing is registered to run
            // before walking in the same Post tick (see LayeredSlopeDynamicPathsMod), so a
            // real landing impact always wins a same-tick conflict and claims the entity
            // first, leaving nothing for a merely-coincidental walking-cadence sample to claim.
            if (chance > 0 && FootfallDedup.claim(entity, tick)) {
                BlockPos under = entity.getOnPos();
                LayerErosion.erode(entity.level(), under, chance, entity.level().getRandom(), true);
            }
        });
    }

    /** Collects into a de-duplicated set first (by entity identity) so an entity visible to more than one nearby player is only processed once per tick. */
    private static void forEachNearbyEntity(MinecraftServer server, java.util.function.Consumer<LivingEntity> action) {
        java.util.Set<LivingEntity> seen = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            AABB area = player.getBoundingBox().inflate(RADIUS);
            seen.addAll(player.serverLevel().getEntitiesOfClass(LivingEntity.class, area));
        }
        seen.forEach(action);
    }
}
