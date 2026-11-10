package io.github.brooswitminecraft.layeredslopedynamicpaths;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Pure per-entity landing state machine backing {@link LandingErosion}, kept Minecraft-type-free
 * (plain {@code Object} keys) so it is directly unit-testable without bootstrapping the game.
 *
 * <p>{@link #observePre} must be called every tick BEFORE this tick's movement/collision
 * resolves, for every tracked entity, with its ground state and downward speed as observed at
 * that moment; {@link #observePostAndConsumeImpact} must be called every tick AFTER movement
 * resolves, with the resulting ground state.
 *
 * <p>Fixes a self-review finding (PR #1 comment 52912): the previous inline tracking in
 * {@code LandingErosion} initialized an entity's prior-ground state to {@code true} the first
 * time it was ever observed (e.g. entering a player's scan radius mid-fall), which silently
 * suppressed that entity's very first landing if it happened to touch down on that same tick.
 * {@link #observePre} now records the entity's ACTUAL observed state via {@code putIfAbsent} the
 * moment it is first seen, so a landing is never missed merely because it was this entity's
 * first tick under observation.
 */
final class LandingTracker {
    private static final Map<Object, Boolean> WAS_ON_GROUND = new WeakHashMap<>();
    private static final Map<Object, Double> LAST_FALL_SPEED = new WeakHashMap<>();

    private LandingTracker() {}

    static void observePre(Object key, boolean onGroundBeforeMovement, double downwardSpeed) {
        WAS_ON_GROUND.putIfAbsent(key, onGroundBeforeMovement);
        if (!onGroundBeforeMovement) {
            LAST_FALL_SPEED.put(key, downwardSpeed);
        }
    }

    /**
     * @return the recorded pre-contact impact speed if {@code key} just transitioned from
     *     airborne to grounded (a fresh landing), or {@code null} if this is not a landing.
     *     Either way, {@code key}'s tracked ground state is updated to {@code onGroundAfterMovement}.
     */
    static Double observePostAndConsumeImpact(Object key, boolean onGroundAfterMovement) {
        boolean wasOnGround = WAS_ON_GROUND.getOrDefault(key, onGroundAfterMovement);
        Double impactSpeed = (onGroundAfterMovement && !wasOnGround) ? LAST_FALL_SPEED.getOrDefault(key, 0.0) : null;
        WAS_ON_GROUND.put(key, onGroundAfterMovement);
        if (onGroundAfterMovement) {
            LAST_FALL_SPEED.remove(key);
        }
        return impactSpeed;
    }
}
