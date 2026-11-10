package io.github.brooswitminecraft.layeredslopedynamicpaths;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LandingMathTest {
    @Test
    void lowImpactSpeedIsZero() {
        assertEquals(0.0, LandingMath.chance(100.0, 0.0));
        assertEquals(0.0, LandingMath.chance(100.0, LandingMath.MIN_IMPACT_SPEED - 0.01));
    }

    @Test
    void nonPositiveMassIsZero() {
        assertEquals(0.0, LandingMath.chance(0.0, 10.0));
    }

    @Test
    void monotonicInImpactSpeedForFixedMass() {
        double low = LandingMath.chance(100.0, 3.0);
        double high = LandingMath.chance(100.0, 20.0);
        assertTrue(low <= high, "chance must not decrease as impact speed increases");
    }

    @Test
    void boundedByMaxChance() {
        double chance = LandingMath.chance(1_000_000.0, 1_000_000.0);
        assertTrue(chance <= LandingMath.MAX_CHANCE);
        assertTrue(chance >= 0.0);
    }

    @Test
    void landingAndWalkingAreIndependentlyTunable() {
        // Same mass and a nominally "fast walk" speed: landing's reference/base differ from
        // walking's, so the two models are free to disagree on a shared input.
        double walking = WalkMath.chance(100.0, 6.0);
        double landing = LandingMath.chance(100.0, 6.0);
        assertTrue(landing > walking, "a 6 m/s impact should erode more than a 6 m/s walking step");
    }
}
