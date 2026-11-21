package net.fastersprint;

import java.util.ArrayDeque;
import java.util.List;

/** Frame-rate-independent, bounded cursor samples. Stationary cursors never accumulate particles. */
public final class CursorTrail {
    public static final long LIFETIME = 180_000_000L;
    private final ArrayDeque<Sample> samples = new ArrayDeque<>();
    private boolean initialized;
    private float previousX, previousY;
    private long previousTime;
    public record Sample(float x, float y, long born) {
        public float remaining(long now) { return Math.clamp(1F - (float) (now - born) / LIFETIME, 0F, 1F); }
    }
    public void update(float x, float y, long now) {
        if (initialized) {
            double distance = Math.hypot(x - previousX, y - previousY);
            if (distance > 200) samples.clear();
            else if (distance >= 1) {
                int steps = Math.max(1, (int) Math.ceil(distance / 2));
                long start = Math.max(previousTime, now - 32_000_000L);
                for (int i = 1; i <= steps; i++) {
                    float t = (float) i / steps;
                    samples.addLast(new Sample(previousX + (x - previousX) * t,
                        previousY + (y - previousY) * t, start + (long) ((now - start) * t)));
                    if (samples.size() > 96) samples.removeFirst();
                }
            }
        }
        initialized = true; previousX = x; previousY = y; previousTime = now;
        prune(now);
    }
    public List<Sample> snapshot(long now) { prune(now); return List.copyOf(samples); }
    private void prune(long now) {
        while (!samples.isEmpty() && now - samples.getFirst().born >= LIFETIME) samples.removeFirst();
    }
    public void clear() { samples.clear(); initialized = false; }
}
