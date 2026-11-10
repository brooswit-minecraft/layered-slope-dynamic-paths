package io.github.brooswitminecraft.layeredslopedynamicpaths;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LandingMathTest {
    private static double chance(double massKg, double impactSpeed) {
        return LandingMath.chance(massKg, impactSpeed, LandingMath.DEFAULT_BASE_CHANCE,
                LandingMath.DEFAULT_REFERENCE_MASS_KG, LandingMath.DEFAULT_REFERENCE_IMPACT_SPEED,
                LandingMath.DEFAULT_MIN_IMPACT_SPEED, LandingMath.DEFAULT_MAX_CHANCE);
    }

    private static double walkChance(double massKg, double speed) {
        return WalkMath.chance(massKg, speed, WalkMath.DEFAULT_BASE_CHANCE, WalkMath.DEFAULT_REFERENCE_MASS_KG,
                WalkMath.DEFAULT_REFERENCE_SPEED, WalkMath.DEFAULT_MIN_SPEED, WalkMath.DEFAULT_MAX_CHANCE);
    }

    @Test
    void lowImpactSpeedIsZero() {
        assertEquals(0.0, chance(100.0, 0.0));
        assertEquals(0.0, chance(100.0, LandingMath.DEFAULT_MIN_IMPACT_SPEED - 0.01));
    }

    @Test
    void nonPositiveMassIsZero() {
        assertEquals(0.0, chance(0.0, 10.0));
    }

    @Test
    void monotonicInImpactSpeedForFixedMass() {
        double low = chance(100.0, 3.0);
        double high = chance(100.0, 20.0);
        assertTrue(low <= high, "chance must not decrease as impact speed increases");
    }

    @Test
    void boundedByMaxChance() {
        double result = chance(1_000_000.0, 1_000_000.0);
        assertTrue(result <= LandingMath.DEFAULT_MAX_CHANCE);
        assertTrue(result >= 0.0);
    }

    @Test
    void landingAndWalkingAreIndependentlyTunableByDefault() {
        // Same mass and a nominally "fast walk" speed: landing's reference/base differ from
        // walking's shipped defaults, so the two models are free to disagree on a shared input.
        double walking = walkChance(100.0, 6.0);
        double landing = chance(100.0, 6.0);
        assertTrue(landing > walking, "a 6 m/s impact should erode more than a 6 m/s walking step");
    }

    /** Each control is a real parameter, not dead config: changing it changes the result. */
    @Test
    void everyControlTakesEffect() {
        double massKg = 100.0;
        double impactSpeed = 6.0;
        double baseline = LandingMath.chance(massKg, impactSpeed, 0.05, 100.0, 6.0, 2.0, 0.9);

        assertTrue(LandingMath.chance(massKg, impactSpeed, 0.5, 100.0, 6.0, 2.0, 1.0) > baseline,
                "raising baseChance must raise the result");
        assertTrue(LandingMath.chance(massKg, impactSpeed, 0.05, 10.0, 6.0, 2.0, 0.9) > baseline,
                "lowering referenceMassKg must raise the result for the same mass");
        assertTrue(LandingMath.chance(massKg, impactSpeed, 0.05, 100.0, 1.0, 2.0, 0.9) > baseline,
                "lowering referenceImpactSpeed must raise the result for the same impact speed");
        assertEquals(0.0, LandingMath.chance(massKg, 6.0, 0.05, 100.0, 6.0, 10.0, 0.9),
                "raising minImpactSpeed above the observed impact speed must zero the result");
        assertTrue(LandingMath.chance(massKg, impactSpeed, 0.05, 100.0, 6.0, 2.0, 0.0001) <= 0.0001,
                "lowering maxChance must cap the result");
    }

    /**
     * Required change (self-review, PR #1): retuning one model's config must never move the
     * other's result for the same inputs -- WalkMath/LandingMath take entirely separate
     * parameter lists, so there is no shared mutable state either could leak through.
     */
    @Test
    void retuningLandingNeverChangesWalking() {
        double massKg = 100.0;
        double speedOrImpact = 6.0;
        double walkingBefore = walkChance(massKg, speedOrImpact);

        // Aggressively retune every landing control away from its default.
        LandingMath.chance(massKg, speedOrImpact, 0.99, 1.0, 0.5, 0.0, 1.0);

        double walkingAfter = walkChance(massKg, speedOrImpact);
        assertEquals(walkingBefore, walkingAfter, "WalkMath's result must be unaffected by any LandingMath call");
    }

    @Test
    void retuningWalkingNeverChangesLanding() {
        double massKg = 100.0;
        double speedOrImpact = 6.0;
        double landingBefore = chance(massKg, speedOrImpact);

        // Aggressively retune every walking control away from its default.
        WalkMath.chance(massKg, speedOrImpact, 0.99, 1.0, 0.5, 0.0, 1.0);

        double landingAfter = chance(massKg, speedOrImpact);
        assertEquals(landingBefore, landingAfter, "LandingMath's result must be unaffected by any WalkMath call");
    }
}
