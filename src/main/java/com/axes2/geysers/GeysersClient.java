package com.axes2.geysers;

import com.axes2.geysers.client.particle.GeyserParticle;
import com.axes2.geysers.config.GeysersClientConfig;
import com.axes2.geysers.registry.ModParticles;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/**
 * Client-only entry point. Registers the client config and the particle providers that
 * give each geyser particle type its look. Explicit {@code Bus.MOD} because the events
 * handled here (client setup, particle-provider registration) are mod-bus events.
 */
@Mod(value = Geysers.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Geysers.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class GeysersClient {

    // --- per-kind particle look (tuning knobs; ARGB tint expressed as 0xRRGGBB) ---
    private static final GeyserParticle.Settings STEAM =
            new GeyserParticle.Settings(-0.02f, 0.96f, false, 40, 30, 0.55f, 0xEFEFEF, 0.60f);
    private static final GeyserParticle.Settings MIST =
            new GeyserParticle.Settings(-0.005f, 0.98f, false, 60, 40, 0.90f, 0xF0F4F8, 0.32f);
    private static final GeyserParticle.Settings SPRAY =
            new GeyserParticle.Settings(0.06f, 0.99f, true, 30, 20, 0.20f, 0xBFD8E6, 0.90f);
    private static final GeyserParticle.Settings BUBBLE =
            new GeyserParticle.Settings(-0.03f, 0.95f, false, 20, 10, 0.15f, 0xCFE6F2, 0.80f);

    public GeysersClient(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, GeysersClientConfig.SPEC);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        Geysers.LOGGER.info("Geysers client setup");
    }

    @SubscribeEvent
    static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.STEAM.get(), sprites -> new GeyserParticle.Provider(sprites, STEAM));
        event.registerSpriteSet(ModParticles.MIST.get(), sprites -> new GeyserParticle.Provider(sprites, MIST));
        event.registerSpriteSet(ModParticles.SPRAY.get(), sprites -> new GeyserParticle.Provider(sprites, SPRAY));
        event.registerSpriteSet(ModParticles.BUBBLE.get(), sprites -> new GeyserParticle.Provider(sprites, BUBBLE));
    }
}
