package com.axes2.geysers.eruption;

/**
 * Pure functions shared by server (authoritative machine) and client (visual
 * re-derivation): given a style and how long the current phase has run, compute the
 * intensity; and given a phase, decide what comes next.
 */
public final class GeyserEruption {
    private GeyserEruption() {}

    /**
     * Intensity at {@code elapsedTicks} into {@code phase}, easing from the intensity the
     * phase started at toward the phase's target. Deterministic, so the client can compute
     * the same value the server would without per-tick syncing.
     */
    public static float computeIntensity(GeyserStyle style, GeyserPhase phase,
                                         long elapsedTicks, float startIntensity) {
        PhaseDef def = style.phase(phase);
        if (def.durationTicks() <= 0) {
            return def.targetIntensity();
        }
        double progress = Math.max(0.0, Math.min(1.0, (double) elapsedTicks / def.durationTicks()));
        double eased = def.easing().apply(progress);
        return (float) (startIntensity + (def.targetIntensity() - startIntensity) * eased);
    }

    /** Default linear cycle, with the optional STEAM branch gated by the style. */
    public static GeyserPhase nextPhase(GeyserPhase phase, GeyserStyle style) {
        return switch (phase) {
            case DORMANT -> GeyserPhase.DORMANT; // waits here until triggered
            case PRIMING -> GeyserPhase.SURGE;
            case SURGE -> GeyserPhase.FULL;
            case FULL -> GeyserPhase.DECLINE;
            case DECLINE -> style.steamPhase() == GeyserStyle.SteamPhaseType.NONE
                    ? GeyserPhase.DISCHARGE : GeyserPhase.STEAM;
            case STEAM -> GeyserPhase.DISCHARGE;
            case DISCHARGE -> GeyserPhase.COOLDOWN;
            case COOLDOWN -> GeyserPhase.DORMANT;
        };
    }
}
