package io.github.brooswitminecraft.layeredslopedynamicpaths;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.jupiter.api.Test;

/**
 * MINECRAFT-556: repository/display-name normalization must never touch the
 * shipped runtime mod ID. These two facts are asserted independently so a
 * future edit to either file trips a test instead of silently drifting.
 */
class MinecraftFiveFiveSixNamingTest {
    @Test
    void shippedModIdIsFrozen() {
        assertEquals("layeredslopedynamicpaths", LayeredSlopeDynamicPathsMod.MODID);
    }

    @Test
    void gradlePropertiesDisplayNameIsTheNormalizedName() throws IOException {
        Properties props = new Properties();
        try (var in = Files.newInputStream(Path.of("gradle.properties"))) {
            props.load(in);
        }
        assertEquals("layeredslopedynamicpaths", props.getProperty("mod_id"));
        assertEquals("Layered Slopes Dynamic Paths", props.getProperty("mod_name"));
    }
}
