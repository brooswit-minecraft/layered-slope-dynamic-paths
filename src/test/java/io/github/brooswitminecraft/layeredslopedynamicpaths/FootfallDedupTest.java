package io.github.brooswitminecraft.layeredslopedynamicpaths;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FootfallDedupTest {
    @Test
    void firstClaimForATickSucceeds() {
        assertTrue(FootfallDedup.claim(new Object(), 1L));
    }

    @Test
    void secondClaimForTheSameKeyAndTickFails() {
        Object key = new Object();
        assertTrue(FootfallDedup.claim(key, 5L));
        assertFalse(FootfallDedup.claim(key, 5L), "a second source must not also claim the same contact this tick");
    }

    @Test
    void theSameKeyCanClaimAgainOnANewTick() {
        Object key = new Object();
        assertTrue(FootfallDedup.claim(key, 5L));
        assertTrue(FootfallDedup.claim(key, 6L), "a new tick is a new contact, not a duplicate");
    }

    @Test
    void differentKeysAreIndependent() {
        Object a = new Object();
        Object b = new Object();
        assertTrue(FootfallDedup.claim(a, 9L));
        assertTrue(FootfallDedup.claim(b, 9L), "one entity's claim must not block a different entity's claim");
    }
}
