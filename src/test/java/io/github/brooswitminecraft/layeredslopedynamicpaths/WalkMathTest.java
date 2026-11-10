package io.github.brooswitminecraft.layeredslopedynamicpaths;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WalkMathTest {
    @Test
    void standingIsZero() {
        assertEquals(0.0, WalkMath.chance(100.0, 0.0));
        assertEquals(0.0, WalkMath.chance(100.0, WalkMath.MIN_SPEED - 0.01));
    }

    @Test
    void nonPositiveMassIsZero() {
        assertEquals(0.0, WalkMath.chance(0.0, 10.0));
        assertEquals(0.0, WalkMath.chance(-5.0, 10.0));
    }

    @Test
    void monotonicInSpeedForFixedMass() {
        double slow = WalkMath.chance(100.0, 1.0);
        double fast = WalkMath.chance(100.0, 5.0);
        assertTrue(slow <= fast, "chance must not decrease as speed increases");
    }

    @Test
    void monotonicInMassForFixedSpeed() {
        double light = WalkMath.chance(50.0, 4.0);
        double heavy = WalkMath.chance(500.0, 4.0);
        assertTrue(light <= heavy, "chance must not decrease as mass increases");
    }

    @Test
    void boundedByMaxChance() {
        double chance = WalkMath.chance(1_000_000.0, 1_000_000.0);
        assertTrue(chance <= WalkMath.MAX_CHANCE);
        assertTrue(chance >= 0.0);
    }
}
