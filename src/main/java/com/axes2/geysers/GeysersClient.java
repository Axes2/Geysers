package com.axes2.geysers;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

// Client-only entry point. Does not load on dedicated servers, so client-only code is safe here.
// Renderer / particle-provider registration is added in M2/M3.
@Mod(value = Geysers.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Geysers.MODID, value = Dist.CLIENT)
public class GeysersClient {

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        Geysers.LOGGER.info("Geysers client setup (Phase 1 scaffold)");
    }
}
