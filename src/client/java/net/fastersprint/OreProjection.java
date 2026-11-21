package net.fastersprint;

import net.minecraft.client.Camera;
import org.joml.Matrix4fc;

/** Five clipping planes, intentionally excluding the vanilla terrain far plane. */
final class OreProjection {
    private final float[] planes = new float[20];
    OreProjection(Matrix4fc m) {
        set(0, m.m03()+m.m00(), m.m13()+m.m10(), m.m23()+m.m20(), m.m33()+m.m30());
        set(1, m.m03()-m.m00(), m.m13()-m.m10(), m.m23()-m.m20(), m.m33()-m.m30());
        set(2, m.m03()+m.m01(), m.m13()+m.m11(), m.m23()+m.m21(), m.m33()+m.m31());
        set(3, m.m03()-m.m01(), m.m13()-m.m11(), m.m23()-m.m21(), m.m33()-m.m31());
        set(4, m.m03(), m.m13(), m.m23(), m.m33()-Camera.PROJECTION_Z_NEAR);
    }
    private void set(int i, float x, float y, float z, float w) {
        int p=i*4; planes[p]=x; planes[p+1]=y; planes[p+2]=z; planes[p+3]=w;
    }
    boolean visible(double x, double y, double z, int size) {
        return visible(x,y,z,size,size,size);
    }
    boolean visible(double x,double y,double z,double width,double height,double depth) {
        for (int p=0;p<20;p+=4) {
            float a=planes[p], b=planes[p+1], c=planes[p+2];
            if (a*(x+(a>0?width:0))+b*(y+(b>0?height:0))+c*(z+(c>0?depth:0))+planes[p+3] < 0) return false;
        }
        return true;
    }
}
