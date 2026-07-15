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

    // --- per-kind particle look (tuning knobs; tint expressed as 0xRRGGBB) ---
    // Settings(gravity, friction, hasPhysics, lifetime, lifetimeJitter, size, tint, alpha, fadeOut)
    private static final GeyserParticle.Settings STEAM =
            new GeyserParticle.Settings(-0.02f, 0.96f, false, 40, 30, 0.55f, 0xEFEFEF, 0.60f, true);
    private static final GeyserParticle.Settings MIST =
            new GeyserParticle.Settings(-0.005f, 0.98f, false, 60, 40, 0.90f, 0xF0F4F8, 0.32f, true);
    // Water: full Minecraft gravity so droplets arc and fall, hasPhysics so they land,
    // long-lived enough to survive the flight, opaque (no fade) so the jet reads as water.
    private static final GeyserParticle.Settings SPRAY =
            new GeyserParticle.Settings(1.0f, 0.98f, true, 50, 30, 0.28f, 0xBFD8E6, 1.0f, false);
    private static final GeyserParticle.Settings BUBBLE =
            new GeyserParticle.Settings(-0.03f, 0.95f, false, 20, 10, 0.15f, 0xCFE6F2, 0.80f, true);

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
