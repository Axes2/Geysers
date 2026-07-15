package com.axes2.geysers.eruption;

/**
 * Interpolation curves used when a phase eases the current intensity toward its target.
 * {@code apply} maps normalized progress [0,1] to an eased [0,1] value.
 */
public enum Easing {
    LINEAR {
        @Override public double apply(double t) { return t; }
    },
    EASE_IN {
        @Override public double apply(double t) { return t * t; }
    },
    EASE_OUT {
        @Override public double apply(double t) { return 1.0 - (1.0 - t) * (1.0 - t); }
    },
    EASE_IN_OUT {
        @Override public double apply(double t) {
            return t < 0.5 ? 2.0 * t * t : 1.0 - Math.pow(-2.0 * t + 2.0, 2.0) / 2.0;
        }
    },
    /** Jumps to the target almost immediately — used for the SURGE launch. */
    SNAP {
        @Override public double apply(double t) { return t <= 0.0 ? 0.0 : 1.0; }
    };

    public abstract double apply(double t);
}
