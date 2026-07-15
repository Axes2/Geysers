package com.axes2.geysers.eruption;

/**
 * Per-phase tuning: how long the phase lasts, the intensity it eases toward, and the
 * curve it uses to get there. Held by {@link GeyserStyle}; the values here are the main
 * timing knobs for watchability.
 */
public record PhaseDef(int durationTicks, float targetIntensity, Easing easing) {
    public static PhaseDef of(int durationTicks, float targetIntensity, Easing easing) {
        return new PhaseDef(durationTicks, targetIntensity, easing);
    }
}
