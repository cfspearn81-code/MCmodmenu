package net.fastersprint;

import java.util.function.DoublePredicate;

/** Geometry shared by placement and movement guarding, without game/runtime dependencies. */
public final class BridgeSafety {
    private BridgeSafety() { }

    public static int footingLayer(double feetY) {
        return (int) Math.floor(feetY) - 1;
    }

    public static double safeFraction(double horizontalDistance, DoublePredicate supported) {
        if (!supported.test(0)) return 0;
        int samples = Math.max(1, (int) Math.ceil(horizontalDistance / 0.05));
        double safe = 0;
        for (int i = 1; i <= samples; i++) {
            double fraction = (double) i / samples;
            if (!supported.test(fraction)) break;
            safe = fraction;
        }
        return safe;
    }
}
