package io.github.brooswitminecraft.layeredslopedynamicpaths;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Entry point for Layered Slope Dynamic Paths (MINECRAFT-512): mob/player footfall and landing
 * erosion of layered and layerable blocks, extracted from Dynamic Terrain's entity-movement
 * erosion. Requires Layered Slopes (blocks only); has no Dynamic Terrain, Dynamic Vehicles or DIV
 * dependency.
 */
@Mod(LayeredSlopeDynamicPathsMod.MODID)
public class LayeredSlopeDynamicPathsMod {
    public static final String MODID = "layeredslopedynamicpaths";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LayeredSlopeDynamicPathsMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, DynamicPathsConfig.SPEC);
        NeoForge.EVENT_BUS.addListener(LandingErosion::onPreTick);
        // Landing's Post listener is registered before walking's (MINECRAFT-512 self-review,
        // FootfallDedup): NeoForge's event bus dispatches same-event listeners in registration
        // order, so a real landing impact always claims an entity's contact before a merely-
        // coincidental walking-cadence sample on the same tick could.
        NeoForge.EVENT_BUS.addListener(LandingErosion::onPostTick);
        NeoForge.EVENT_BUS.addListener(WalkErosion::onServerTick);
        LOGGER.info("Layered Slope Dynamic Paths loaded");
    }
}
