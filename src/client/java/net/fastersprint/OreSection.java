package net.fastersprint;

import java.util.Arrays;

/** Compact local positions and ore ordinals; no BlockPos per ore. Client-thread owned. */
final class OreSection {
    private int[] entries = new int[0];
    private int size;
    private short[] byLocal;
    int size() { return size; }
    int entry(int index) { return entries[index]; }
    void add(int local, int type) {
        if (size == entries.length) entries = Arrays.copyOf(entries, Math.max(16, size * 2));
        entries[size++] = local | (type << 12);
        if (byLocal != null) byLocal[local] = (short) size;
    }
    private void indexDense() {
        // Large veins receive constant-time updates; sparse sections keep the smaller representation.
        if (byLocal == null && size >= 128) {
            byLocal = new short[4096];
            for (int i=0;i<size;i++) byLocal[entries[i]&4095]=(short)(i+1);
        }
    }
    int typeAt(int local) {
        indexDense();
        if (byLocal != null) { int i=byLocal[local]-1; return i<0?-1:entries[i]>>>12; }
        for (int i=0;i<size;i++) if ((entries[i]&4095)==local) return entries[i]>>>12;
        return -1;
    }
    boolean update(int local, int type) {
        indexDense();
        int found = byLocal == null ? -1 : byLocal[local]-1;
        if (byLocal == null) for (int i=0;i<size;i++) if ((entries[i]&4095)==local) { found=i;break; }
        if (found >= 0) {
            if ((entries[found] >>> 12) == type) return false;
            if (type < 0) {
                entries[found] = entries[--size];
                if (byLocal != null) {
                    byLocal[local]=0;
                    if (found < size) byLocal[entries[found]&4095]=(short)(found+1);
                }
            } else entries[found] = local | (type << 12);
            return true;
        }
        if (type < 0) return false;
        add(local, type); return true;
    }
}
