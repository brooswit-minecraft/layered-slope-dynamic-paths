package io.github.brooswitminecraft.layeredslopedynamicpaths;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

/**
 * Entry point for Layered Slope Dynamic Paths. Registers the mod with no gameplay logic;
 * add your own event listeners, registries, and config here.
 */
@Mod(LayeredSlopeDynamicPathsMod.MODID)
public class LayeredSlopeDynamicPathsMod {
    public static final String MODID = "layeredslopedynamicpaths";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LayeredSlopeDynamicPathsMod(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Layered Slope Dynamic Paths loaded");
    }
}
