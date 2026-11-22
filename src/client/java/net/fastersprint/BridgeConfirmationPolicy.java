package net.fastersprint;

/** Learn from sustained confirmed footing, never from ACKs or locally predicted blocks. */
public final class BridgeConfirmationPolicy {
    private int stablePlacements;
    private long recoveryUntil;
    public int settleTicks() { return stablePlacements >= 3 ? 1 : 10; }
    public void stable(long tick) { if (tick >= recoveryUntil) stablePlacements = Math.min(3, stablePlacements + 1); }
    public void rejected(long tick) { stablePlacements = 0; recoveryUntil = tick + 100; }
    public void clear() { stablePlacements = 0; recoveryUntil = 0; }
}
