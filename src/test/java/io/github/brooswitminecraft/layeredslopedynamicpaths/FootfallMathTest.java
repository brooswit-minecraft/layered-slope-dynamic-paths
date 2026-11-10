package io.github.brooswitminecraft.layeredslopedynamicpaths;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FootfallMathTest {
    @Test
    void massScalesWithVolume() {
        double small = FootfallMath.massKg(0.6, 1.8);
        double large = FootfallMath.massKg(1.5, 3.0);
        assertTrue(small < large);
    }

    @Test
    void negativeHeightNeverProducesNegativeMass() {
        assertEquals(0.0, FootfallMath.massKg(1.0, -1.0));
    }
}
