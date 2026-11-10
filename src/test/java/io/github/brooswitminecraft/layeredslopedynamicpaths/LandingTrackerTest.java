package io.github.brooswitminecraft.layeredslopedynamicpaths;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class LandingTrackerTest {
    /**
     * Regression for self-review 52912: a never-before-seen entity observed mid-fall (pre-tick
     * airborne) that lands THIS SAME TICK must still be detected as a fresh landing. Under the
     * old buggy default ("assume already grounded" for an unseen entity), this was silently
     * missed.
     */
    @Test
    void aNewlyObservedAirborneEntityLandingOnItsFirstTickIsDetected() {
        Object entity = new Object();
        LandingTracker.observePre(entity, false, 8.0);
        Double impact = LandingTracker.observePostAndConsumeImpact(entity, true);
        assertNotNull(impact, "a first-ever-observed entity's first landing must not be missed");
        assertEquals(8.0, impact);
    }

    @Test
    void aNeverAirborneEntityNeverLands() {
        Object entity = new Object();
        LandingTracker.observePre(entity, true, 0.0);
        assertNull(LandingTracker.observePostAndConsumeImpact(entity, true),
                "an entity observed already on the ground must not register a landing");
    }

    @Test
    void stayingAirborneAcrossSeveralTicksNeverLandsUntilItTouchesDown() {
        Object entity = new Object();
        LandingTracker.observePre(entity, false, 2.0);
        assertNull(LandingTracker.observePostAndConsumeImpact(entity, false), "still falling: not a landing yet");

        LandingTracker.observePre(entity, false, 5.0);
        Double impact = LandingTracker.observePostAndConsumeImpact(entity, true);
        assertNotNull(impact, "the tick it actually touches down must register as a landing");
        assertEquals(5.0, impact, "the impact speed recorded just before contact, not an earlier sample, must be used");
    }

    @Test
    void theSameLandingIsNotReportedTwice() {
        Object entity = new Object();
        LandingTracker.observePre(entity, false, 8.0);
        assertNotNull(LandingTracker.observePostAndConsumeImpact(entity, true));

        // Still grounded next tick: must not register as a second landing.
        LandingTracker.observePre(entity, true, 0.0);
        assertNull(LandingTracker.observePostAndConsumeImpact(entity, true));
    }

    @Test
    void anEntityCanLandAgainAfterLeavingTheGroundAgain() {
        Object entity = new Object();
        LandingTracker.observePre(entity, false, 8.0);
        assertNotNull(LandingTracker.observePostAndConsumeImpact(entity, true), "first landing");

        LandingTracker.observePre(entity, true, 0.0);
        assertNull(LandingTracker.observePostAndConsumeImpact(entity, false), "jumped back into the air");

        LandingTracker.observePre(entity, false, 3.0);
        assertNotNull(LandingTracker.observePostAndConsumeImpact(entity, true), "second landing must register too");
    }

    @Test
    void differentEntitiesAreTrackedIndependently() {
        Object a = new Object();
        Object b = new Object();
        LandingTracker.observePre(a, false, 10.0);
        LandingTracker.observePre(b, true, 0.0);
        assertNotNull(LandingTracker.observePostAndConsumeImpact(a, true), "a landed");
        assertNull(LandingTracker.observePostAndConsumeImpact(b, true), "b never left the ground");
    }
}
