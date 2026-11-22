package net.fastersprint;

import java.util.HashMap;
import java.util.Map;
import java.util.function.LongConsumer;
import java.util.function.LongPredicate;

/** Keep recent server-confirmed footing under observation after allowing movement onto it. */
public final class BridgeFootingTracker {
    private static final long UNVERIFIED = Long.MIN_VALUE;
    private static final int STABLE_OBSERVATION_TICKS = 20, WATCH_TICKS = 100;
    private static final class Footing {
        long confirmedAt = UNVERIFIED, approvedAt = UNVERIFIED;
        boolean reportedStable;
    }
    private final Map<Long, Footing> footing = new HashMap<>();

    public void predicted(long position) { footing.put(position, new Footing()); }
    /** True once when server feedback revokes an earlier solid confirmation. */
    public boolean serverUpdate(long position, boolean solid, long tick) {
        Footing entry = footing.get(position);
        if (entry == null) return false;
        boolean revoked = !solid && entry.confirmedAt != UNVERIFIED;
        if (!solid) { entry.confirmedAt = UNVERIFIED; entry.approvedAt = UNVERIFIED; entry.reportedStable = false; }
        else if (entry.confirmedAt == UNVERIFIED) entry.confirmedAt = tick;
        return revoked;
    }
    public boolean blocked(long position, long tick, int settleTicks) {
        Footing entry = footing.get(position);
        return entry != null && entry.approvedAt == UNVERIFIED
            && (entry.confirmedAt == UNVERIFIED || tick - entry.confirmedAt < settleTicks);
    }
    public void approve(long position, long tick) {
        Footing entry = footing.get(position);
        if (entry != null && entry.confirmedAt != UNVERIFIED && entry.approvedAt == UNVERIFIED) entry.approvedAt = tick;
    }
    public void prune(long tick, int settleTicks, LongPredicate stillPredicted, LongPredicate empty) {
        prune(tick, settleTicks, stillPredicted, empty, ignored -> { });
    }
    /** Learn only once per stable placement; retain rollback observation for five seconds. */
    public void prune(long tick, int settleTicks, LongPredicate stillPredicted, LongPredicate empty, LongConsumer stable) {
        footing.entrySet().removeIf(item -> {
            long position = item.getKey(); Footing entry = item.getValue();
            if (stillPredicted.test(position)) return false;
            if (empty.test(position)) return true;
            if (blocked(position, tick, settleTicks)) return false;
            approve(position, tick);
            if (!entry.reportedStable && tick - entry.approvedAt >= STABLE_OBSERVATION_TICKS) {
                entry.reportedStable = true; stable.accept(tick);
            }
            return tick - entry.approvedAt >= WATCH_TICKS;
        });
    }
    public void removeIf(LongPredicate removed) { footing.keySet().removeIf(removed::test); }
    public void clear() { footing.clear(); }
}
