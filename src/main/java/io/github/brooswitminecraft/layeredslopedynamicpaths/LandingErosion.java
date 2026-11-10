package io.github.brooswitminecraft.layeredslopedynamicpaths;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Landing entities as an erosion source, distinct from walking. Captures the PRE-contact impact
 * velocity rather than the post-landing (already zeroed) velocity: {@link #onPreTick} records
 * each airborne entity's current downward speed before this tick's movement/collision runs, and
 * {@link #onPostTick} consumes that recorded speed the instant {@code onGround()} transitions
 * from false to true, then clears it so the same landing cannot erode twice. Entries live in
 * {@link WeakHashMap}s keyed by entity identity, so a despawned/unloaded entity's state is
 * reclaimed automatically without an explicit unbounded cleanup pass.
 */
public final class LandingErosion {
    private static final double RADIUS = 24.0;
    private static final Map<LivingEntity, Double> LAST_FALL_SPEED = new WeakHashMap<>();
    private static final Map<LivingEntity, Boolean> WAS_ON_GROUND = new WeakHashMap<>();

    private LandingErosion() {}

    public static void onPreTick(ServerTickEvent.Pre event) {
        if (!DynamicPathsConfig.EROSION_ENABLED.get() || !DynamicPathsConfig.LANDING_EROSION.get()) {
            return;
        }
        forEachNearbyEntity(event.getServer(), entity -> {
            if (!entity.onGround()) {
                LAST_FALL_SPEED.put(entity, -entity.getDeltaMovement().y);
            }
        });
    }

    public static void onPostTick(ServerTickEvent.Post event) {
        if (!DynamicPathsConfig.EROSION_ENABLED.get() || !DynamicPathsConfig.LANDING_EROSION.get()) {
            return;
        }
        forEachNearbyEntity(event.getServer(), entity -> {
            boolean onGround = entity.onGround();
            boolean wasOnGround = WAS_ON_GROUND.getOrDefault(entity, true);
            if (onGround && !wasOnGround && !entity.isSpectator()) {
                double impactSpeed = LAST_FALL_SPEED.getOrDefault(entity, 0.0);
                double massKg = FootfallMath.massKg(entity.getBbWidth(), entity.getBbHeight());
                double chance = LandingMath.chance(massKg, impactSpeed);
                if (chance > 0) {
                    BlockPos under = entity.getOnPos();
                    LayerErosion.erode(entity.level(), under, chance, entity.level().getRandom(), true);
                }
            }
            WAS_ON_GROUND.put(entity, onGround);
            if (onGround) {
                LAST_FALL_SPEED.remove(entity);
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
