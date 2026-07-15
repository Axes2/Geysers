package com.axes2.geysers;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import com.axes2.geysers.registry.ModBlockEntities;
import com.axes2.geysers.registry.ModBlocks;
import com.axes2.geysers.registry.ModItems;
import com.axes2.geysers.registry.ModParticles;
import com.axes2.geysers.registry.ModSounds;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

// The value here must match the modId in gradle.properties / neoforge.mods.toml.
@Mod(Geysers.MODID)
public class Geysers {
    public static final String MODID = "geysers";
    public static final Logger LOGGER = LogUtils.getLogger();

    // FML recognizes IEventBus / ModContainer parameter types and injects them automatically.
    public Geysers(IEventBus modEventBus, ModContainer modContainer) {
        // Register all deferred registries to the mod event bus. The holder classes are empty
        // scaffolds in M0; entries get added as later milestones land (blocks/BE in M1,
        // particles in M2, sounds in M5, etc.). The wiring here does not change.
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModParticles.register(modEventBus);
        ModSounds.register(modEventBus);

        LOGGER.info("Geysers initializing (Phase 1 scaffold)");
    }
}
