package com.axes2.geysers.eruption;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * One engine, many looks. Holds every tunable parameter for a geyser: per-phase timing
 * plus the visual mix. In M1 these are code-defined defaults; in M2 the same shape is
 * loaded from hot-reloadable datapack JSON so values can be tuned without restarting.
 *
 * <p>Visual fields (height/width/weights/tint) are declared now but only consumed once
 * the particle layer lands in M2, so the type does not churn between milestones.
 */
public record GeyserStyle(
        Map<GeyserPhase, PhaseDef> phases,
        float maxColumnHeight,
        float columnRadius,
        float taper,
        float burstiness,
        float sprayWeight,
        float steamWeight,
        float mistWeight,
        int waterTint,
        SteamPhaseType steamPhase,
        boolean blueBubble
) {
    public enum SteamPhaseType { NONE, PASSIVE, VIOLENT }

    /** Fallback timing used when a style omits a phase. */
    public static final Map<GeyserPhase, PhaseDef> DEFAULT_PHASES;

    static {
        EnumMap<GeyserPhase, PhaseDef> map = new EnumMap<>(GeyserPhase.class);
        map.put(GeyserPhase.DORMANT,   PhaseDef.of(0,   0.00f, Easing.LINEAR));
        map.put(GeyserPhase.PRIMING,   PhaseDef.of(200, 0.35f, Easing.EASE_IN));
        map.put(GeyserPhase.SURGE,     PhaseDef.of(10,  1.00f, Easing.SNAP));
        map.put(GeyserPhase.FULL,      PhaseDef.of(120, 1.00f, Easing.LINEAR));
        map.put(GeyserPhase.DECLINE,   PhaseDef.of(80,  0.30f, Easing.EASE_IN_OUT));
        map.put(GeyserPhase.STEAM,     PhaseDef.of(120, 0.50f, Easing.EASE_OUT));
        map.put(GeyserPhase.DISCHARGE, PhaseDef.of(60,  0.12f, Easing.EASE_OUT));
        map.put(GeyserPhase.COOLDOWN,  PhaseDef.of(60,  0.00f, Easing.EASE_IN_OUT));
        DEFAULT_PHASES = Collections.unmodifiableMap(map);
    }

    public PhaseDef phase(GeyserPhase phase) {
        return phases.getOrDefault(phase, DEFAULT_PHASES.get(phase));
    }

    /** A general-purpose default (fountain-leaning) used until real presets arrive. */
    public static GeyserStyle defaultStyle() {
        return new GeyserStyle(
                DEFAULT_PHASES,
                8.0f,   // maxColumnHeight (blocks)
                0.6f,   // columnRadius
                0.5f,   // taper
                0.5f,   // burstiness
                0.5f,   // sprayWeight
                0.5f,   // steamWeight
                0.5f,   // mistWeight
                0xFFBFD8E6, // waterTint (ARGB, pale blue)
                SteamPhaseType.PASSIVE,
                true    // blueBubble
        );
    }
}
