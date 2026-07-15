package com.axes2.geysers.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Client-side visual tuning. NeoForge reloads this when the toml file changes, so values
 * can be adjusted live while the game runs. This is the fast global knob set; per-style
 * parameters live in {@code GeyserStyle} (datapack JSON lands in M2b).
 */
public final class GeysersClientConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.DoubleValue DENSITY_MULTIPLIER;
    private static final ModConfigSpec.IntValue MAX_PARTICLES_PER_VENT_PER_TICK;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("particles");
        DENSITY_MULTIPLIER = builder
                .comment("Global multiplier on all geyser particle spawn counts.")
                .defineInRange("densityMultiplier", 1.0, 0.0, 8.0);
        MAX_PARTICLES_PER_VENT_PER_TICK = builder
                .comment("Hard cap on particles spawned by a single vent each client tick.")
                .defineInRange("maxParticlesPerVentPerTick", 60, 1, 2000);
        builder.pop();
        SPEC = builder.build();
    }

    private GeysersClientConfig() {}

    public static double densityMultiplier() {
        return DENSITY_MULTIPLIER.get();
    }

    public static int maxParticlesPerVentPerTick() {
        return MAX_PARTICLES_PER_VENT_PER_TICK.get();
    }
}
