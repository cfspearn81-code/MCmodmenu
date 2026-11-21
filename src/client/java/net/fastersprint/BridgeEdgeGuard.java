package net.fastersprint;

import java.util.function.DoublePredicate;

/** Shared swept-path decision used for input prediction and the final vanilla movement step. */
public final class BridgeEdgeGuard {
    private BridgeEdgeGuard() { }
    public static double fraction(double distance, boolean fullFootSupported, DoublePredicate stableSupport) {
        // A server correction can leave the player at the margin. Allow a short step back to safety.
        if (!stableSupport.test(0) && fullFootSupported && distance <= 0.5 && stableSupport.test(1)) return 1;
        return BridgeSafety.safeFraction(distance, stableSupport);
    }
}
