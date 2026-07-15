package com.axes2.geysers.registry;

import com.axes2.geysers.Geysers;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Particle-type registry. Steam, spray, mist, splash and runoff types are added in M2.
 * Client-side particle providers are registered separately in {@code GeysersClient} via
 * {@code RegisterParticleProvidersEvent}.
 */
public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, Geysers.MODID);

    private ModParticles() {}

    public static void register(IEventBus modEventBus) {
        PARTICLE_TYPES.register(modEventBus);
    }
}
