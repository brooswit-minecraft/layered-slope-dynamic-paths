package io.github.brooswitminecraft.layeredslopedynamicpaths;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Cross-source per-tick de-duplication, shared by {@link WalkErosion} and {@link LandingErosion}.
 * Each source only de-duplicated entities against itself; a landing that happens to fall on a
 * walking-cadence tick could still be eroded once by each independent source for the same
 * contact (self-review, PR #1 comment 52901). Both sources now {@link #claim} the entity for the
 * current tick before eroding; whichever source runs first for that tick wins, the other skips.
 * Landing is registered first (see {@code LayeredSlopeDynamicPathsMod}) so a real landing impact
 * always wins a same-tick conflict over a merely-coincidental walking-cadence sample.
 *
 * <p>Keyed by plain identity (a {@code LivingEntity} never overrides {@code equals}/
 * {@code hashCode}), so a {@link WeakHashMap} reclaims entries for despawned/unloaded entities
 * automatically without an explicit cleanup pass -- same precedent as {@link LandingErosion}'s
 * own tracking maps.
 */
final class FootfallDedup {
    private static final Map<Object, Long> LAST_CLAIMED_TICK = new WeakHashMap<>();

    private FootfallDedup() {}

    /** @return true if {@code key} had not already claimed {@code tick}; false if it already had. */
    static boolean claim(Object key, long tick) {
        Long previous = LAST_CLAIMED_TICK.put(key, tick);
        return previous == null || previous != tick;
    }
}
