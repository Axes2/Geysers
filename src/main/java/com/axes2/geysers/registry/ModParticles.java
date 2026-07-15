package com.axes2.geysers.registry;

import com.axes2.geysers.Geysers;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Particle-type registry (common). Client-side providers that give these types their
 * look are registered separately in {@code GeysersClient} via
 * {@code RegisterParticleProvidersEvent}.
 */
public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, Geysers.MODID);

    /** Buoyant billows rising off the vent and column. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> STEAM = simple("steam");
    /** Ballistic water droplets that arc up and fall back. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SPRAY = simple("spray");
    /** Slow, wide, low-opacity fog that drifts and fades. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MIST = simple("mist");
    /** Rising bubbles in the pool during priming. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BUBBLE = simple("bubble");

    private ModParticles() {}

    private static DeferredHolder<ParticleType<?>, SimpleParticleType> simple(String name) {
        // SimpleParticleType's constructor is protected; an anonymous subclass exposes it.
        return PARTICLE_TYPES.register(name, () -> new SimpleParticleType(false) {});
    }

    public static void register(IEventBus modEventBus) {
        PARTICLE_TYPES.register(modEventBus);
    }
}
