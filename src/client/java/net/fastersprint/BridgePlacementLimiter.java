package net.fastersprint;

/** A single budget for successful, failed and locally rejected interaction attempts. */
public final class BridgePlacementLimiter {
    private long nextAttempt;
    private int rejections;

    public boolean ready(long tick) { return rejections < 3 && tick >= nextAttempt; }
    public boolean blocked() { return rejections >= 3; }

    public void attempted(long tick) {
        // Vanilla held-use interval: at most five auto-placement attempts per second at 20 TPS.
        nextAttempt = Math.max(nextAttempt, tick + 4);
    }

    public void accepted() { rejections = 0; }

    public void rejected(long tick) {
        rejections = Math.min(3, rejections + 1);
        nextAttempt = Math.max(nextAttempt, tick + (20L << (rejections - 1)));
    }

    public void released() {
        rejections = 0;
        // Do not reset the deadline: repeatedly tapping the key must not bypass the rate limit.
    }
}
