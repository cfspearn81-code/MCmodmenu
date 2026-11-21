package net.fastersprint;

/** Fully charged weapon attacks, with one attempt per weapon cooldown even on local refusal. */
public final class AttackCadence {
    private long nextAttempt;
    public boolean reserve(long tick, float strength, float delay) {
        if (!Float.isFinite(strength) || strength < 1 || tick < nextAttempt) return false;
        long interval = Float.isFinite(delay) ? (long) Math.ceil(Math.max(1, delay)) : 20;
        nextAttempt = tick + interval;
        return true;
    }
}
