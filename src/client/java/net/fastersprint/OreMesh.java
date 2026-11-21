package net.fastersprint;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

/** Immutable, deduplicated unit edges. Relative coordinates use 14 bits per axis. */
final class OreMesh {
    static final OreMesh EMPTY = new OreMesh(0,0,0,new long[0]);
    final int x, y, z;
    final long[] edges;
    OreMesh(int x,int y,int z,long[] edges) { this.x=x;this.y=y;this.z=z;this.edges=edges; }
    static long edge(int x,int y,int z,int axis,int type) {
        return (long)(x+8192) | ((long)(y+8192)<<14) | ((long)(z+8192)<<28) | ((long)axis<<42) | ((long)type<<44);
    }
    static int x(long edge) { return (int)(edge&16383)-8192; }
    static int y(long edge) { return (int)((edge>>>14)&16383)-8192; }
    static int z(long edge) { return (int)((edge>>>28)&16383)-8192; }
    static int axis(long edge) { return (int)((edge>>>42)&3); }
    static int type(long edge) { return (int)(edge>>>44); }
    static void addBox(LongOpenHashSet edges,int x,int y,int z,int type) {
        for (int corner=0;corner<8;corner++) for (int axis=0;axis<3;axis++) {
            if ((corner&(1<<axis))!=0) continue;
            edges.add(edge(x+(corner&1),y+((corner>>>1)&1),z+((corner>>>2)&1),axis,type));
        }
    }
}
