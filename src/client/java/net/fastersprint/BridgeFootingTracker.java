package net.fastersprint;

import java.util.HashMap;
import java.util.Map;
import java.util.function.LongPredicate;

/** Predictions remain quarantined until a distinct server block update confirms solid footing. */
public final class BridgeFootingTracker {
    private static final long UNVERIFIED = Long.MIN_VALUE;
    private static final class Footing { long confirmedAt = UNVERIFIED; }
    private final Map<Long, Footing> footing = new HashMap<>();

    public void predicted(long position) { footing.put(position, new Footing()); }
    public void serverUpdate(long position, boolean solid, long tick) {
        Footing entry = footing.get(position);
        if (entry == null) return;
        if (!solid) entry.confirmedAt = UNVERIFIED;
        else if (entry.confirmedAt == UNVERIFIED) entry.confirmedAt = tick;
    }
    public boolean blocked(long position, long tick, int settleTicks) {
        Footing entry = footing.get(position);
        return entry != null && (entry.confirmedAt == UNVERIFIED || tick - entry.confirmedAt < settleTicks);
    }
    /** Discard confirmed/stable entries only when no newer vanilla prediction obscures them. */
    public void prune(long tick, int settleTicks, LongPredicate stillPredicted, LongPredicate empty) {
        footing.entrySet().removeIf(entry -> !stillPredicted.test(entry.getKey())
            && (empty.test(entry.getKey()) || !blocked(entry.getKey(), tick, settleTicks)));
    }
    public void removeIf(LongPredicate removed) { footing.keySet().removeIf(removed::test); }
    public void clear() { footing.clear(); }
}
