package com.axes2.geysers.eruption;

import net.minecraft.util.StringRepresentable;

/**
 * The stages of a single eruption cycle. Each phase declares (via {@link GeyserStyle})
 * a target intensity, a duration, and an easing curve; the per-tick intensity eases
 * toward the target so visuals stay continuous rather than snapping between states.
 */
public enum GeyserPhase implements StringRepresentable {
    DORMANT("dormant"),
    PRIMING("priming"),
    SURGE("surge"),
    FULL("full"),
    DECLINE("decline"),
    STEAM("steam"),
    DISCHARGE("discharge"),
    COOLDOWN("cooldown");

    private final String serializedName;

    GeyserPhase(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public static GeyserPhase byName(String name) {
        for (GeyserPhase phase : values()) {
            if (phase.serializedName.equals(name)) {
                return phase;
            }
        }
        return null;
    }
}
