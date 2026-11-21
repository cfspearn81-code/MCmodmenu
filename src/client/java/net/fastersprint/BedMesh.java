package net.fastersprint;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

/** Reuses Xray's packed coordinate layout, with flags for bed height and two-block edges. */
final class BedMesh {
    static final long TOP = 1L << 48, DOUBLE = 1L << 49;
    static final BedMesh EMPTY = new BedMesh(0,0,0,new long[0],0);
    final int x,y,z,beds;
    final long[] edges;
    BedMesh(int x,int y,int z,long[] edges,int beds) { this.x=x;this.y=y;this.z=z;this.edges=edges;this.beds=beds; }
    static void addBox(LongOpenHashSet edges,int x,int y,int z,int width,int depth,int color) {
        for (int corner=0;corner<8;corner++) for (int axis=0;axis<3;axis++) {
            if ((corner & (1 << axis)) != 0) continue;
            long edge = OreMesh.edge(x+((corner&1)!=0?width:0), y, z+((corner&4)!=0?depth:0),axis,color);
            if ((corner&2)!=0) edge |= TOP;
            if ((axis==0&&width==2)||(axis==2&&depth==2)) edge |= DOUBLE;
            edges.add(edge);
        }
    }
    static int color(long edge) { return (int)(edge >>> 44) & 15; }
    static float y(long edge) { return (float)(OreMesh.y(edge) + ((edge&TOP)!=0?BedGeometry.HEIGHT:0)); }
    static float length(long edge) { return OreMesh.axis(edge)==1?(float)BedGeometry.HEIGHT:(edge&DOUBLE)!=0?2:1; }
}
