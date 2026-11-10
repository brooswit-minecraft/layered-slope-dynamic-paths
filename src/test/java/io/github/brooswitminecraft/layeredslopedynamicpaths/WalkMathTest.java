package io.github.brooswitminecraft.layeredslopedynamicpaths;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WalkMathTest {
    private static double chance(double massKg, double speed) {
        return WalkMath.chance(massKg, speed, WalkMath.DEFAULT_BASE_CHANCE, WalkMath.DEFAULT_REFERENCE_MASS_KG,
                WalkMath.DEFAULT_REFERENCE_SPEED, WalkMath.DEFAULT_MIN_SPEED, WalkMath.DEFAULT_MAX_CHANCE);
    }

    @Test
    void standingIsZero() {
        assertEquals(0.0, chance(100.0, 0.0));
        assertEquals(0.0, chance(100.0, WalkMath.DEFAULT_MIN_SPEED - 0.01));
    }

    @Test
    void nonPositiveMassIsZero() {
        assertEquals(0.0, chance(0.0, 10.0));
        assertEquals(0.0, chance(-5.0, 10.0));
    }

    @Test
    void monotonicInSpeedForFixedMass() {
        double slow = chance(100.0, 1.0);
        double fast = chance(100.0, 5.0);
        assertTrue(slow <= fast, "chance must not decrease as speed increases");
    }

    @Test
    void monotonicInMassForFixedSpeed() {
        double light = chance(50.0, 4.0);
        double heavy = chance(500.0, 4.0);
        assertTrue(light <= heavy, "chance must not decrease as mass increases");
    }

    @Test
    void boundedByMaxChance() {
        double result = chance(1_000_000.0, 1_000_000.0);
        assertTrue(result <= WalkMath.DEFAULT_MAX_CHANCE);
        assertTrue(result >= 0.0);
    }

    /** Each control is a real parameter, not dead config: changing it changes the result. */
    @Test
    void everyControlTakesEffect() {
        double massKg = 100.0;
        double speed = 4.0;
        double baseline = WalkMath.chance(massKg, speed, 0.01, 100.0, 4.0, 0.5, 0.25);

        assertTrue(WalkMath.chance(massKg, speed, 0.5, 100.0, 4.0, 0.5, 1.0) > baseline,
                "raising baseChance must raise the result");
        assertTrue(WalkMath.chance(massKg, speed, 0.01, 10.0, 4.0, 0.5, 0.25) > baseline,
                "lowering referenceMassKg must raise the result for the same mass");
        assertTrue(WalkMath.chance(massKg, speed, 0.01, 100.0, 1.0, 0.5, 0.25) > baseline,
                "lowering referenceSpeed must raise the result for the same speed");
        assertEquals(0.0, WalkMath.chance(massKg, 6.0, 0.01, 100.0, 4.0, 10.0, 0.25),
                "raising minSpeed above the observed speed must zero the result");
        assertTrue(WalkMath.chance(massKg, speed, 0.01, 100.0, 4.0, 0.5, 0.0001) <= 0.0001,
                "lowering maxChance must cap the result");
    }
}
