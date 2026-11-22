package net.fastersprint;

import java.util.HashMap;
import java.util.Map;
import java.util.function.LongPredicate;

/** Keep recent server-confirmed footing under observation after allowing movement onto it. */
public final class BridgeFootingTracker {
    private static final long UNVERIFIED = Long.MIN_VALUE;
    private static final int WATCH_TICKS = 100;
    private static final class Footing {
        long confirmedAt = UNVERIFIED, approvedAt = UNVERIFIED;
    }
    private final Map<Long, Footing> footing = new HashMap<>();

    public void predicted(long position) { footing.put(position, new Footing()); }
    /** True once when server feedback revokes an earlier solid confirmation. */
    public boolean serverUpdate(long position, boolean solid, long tick) {
        Footing entry = footing.get(position);
        if (entry == null) return false;
        boolean revoked = !solid && entry.confirmedAt != UNVERIFIED;
        if (!solid) { entry.confirmedAt = UNVERIFIED; entry.approvedAt = UNVERIFIED; }
        else if (entry.confirmedAt == UNVERIFIED) entry.confirmedAt = tick;
        return revoked;
    }
    public boolean blocked(long position, long tick, int settleTicks) {
        Footing entry = footing.get(position);
        return entry != null && entry.approvedAt == UNVERIFIED && !confirmed(position, tick, settleTicks);
    }
    /** A missing record is not proof that a pending placement reached the server. */
    public boolean confirmed(long position, long tick, int settleTicks) {
        Footing entry = footing.get(position);
        return entry != null && entry.confirmedAt != UNVERIFIED && tick - entry.confirmedAt >= settleTicks;
    }
    public void approve(long position, long tick) {
        Footing entry = footing.get(position);
        if (entry != null && entry.confirmedAt != UNVERIFIED && entry.approvedAt == UNVERIFIED) entry.approvedAt = tick;
    }
    public void prune(long tick, int settleTicks, LongPredicate stillPredicted, LongPredicate empty) {
        // Retain rollback observation for five seconds after approval.
        footing.entrySet().removeIf(item -> {
            long position = item.getKey(); Footing entry = item.getValue();
            if (stillPredicted.test(position)) return false;
            if (empty.test(position)) return true;
            if (blocked(position, tick, settleTicks)) return false;
            approve(position, tick);
            return tick - entry.approvedAt >= WATCH_TICKS;
        });
    }
    public void removeIf(LongPredicate removed) { footing.keySet().removeIf(removed::test); }
    public void clear() { footing.clear(); }
}
