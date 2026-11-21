package net.fastersprint;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import org.joml.Matrix3x2fc;

final class OutlineLine {
    private OutlineLine() { }
    static void emit(VertexConsumer out, Matrix3x2fc pose, float ax, float ay, float aw,
            float bx, float by, float bw, float halfWidth, float halfHeight, int color) {
        float start=0,end=1;
        boolean rejected=false;
        for(int plane=0;plane<5;plane++) {
            float a=switch(plane){case 0->aw-Camera.PROJECTION_Z_NEAR;case 1->aw+ax;case 2->aw-ax;case 3->aw+ay;default->aw-ay;};
            float b=switch(plane){case 0->bw-Camera.PROJECTION_Z_NEAR;case 1->bw+bx;case 2->bw-bx;case 3->bw+by;default->bw-by;};
            if(a<0&&b<0){rejected=true;break;}
            if((a<0)!=(b<0)){float fraction=a/(a-b);if(a<0)start=Math.max(start,fraction);else end=Math.min(end,fraction);}
        }
        if(rejected||start>end)return;
        float firstW=aw+(bw-aw)*start,secondW=aw+(bw-aw)*end;
        float x0=(ax+(bx-ax)*start)/firstW*halfWidth+halfWidth;
        float y0=halfHeight-(ay+(by-ay)*start)/firstW*halfHeight;
        float x1=(ax+(bx-ax)*end)/secondW*halfWidth+halfWidth;
        float y1=halfHeight-(ay+(by-ay)*end)/secondW*halfHeight;
        float dx=x1-x0,dy=y1-y0,length2=dx*dx+dy*dy;
        if(length2<.000001F)return;
        float scale=.5F/(float)Math.sqrt(length2),nx=-dy*scale,ny=dx*scale;
        out.addVertexWith2DPose(pose,x0-nx,y0-ny).setColor(color);
        out.addVertexWith2DPose(pose,x0+nx,y0+ny).setColor(color);
        out.addVertexWith2DPose(pose,x1+nx,y1+ny).setColor(color);
        out.addVertexWith2DPose(pose,x1-nx,y1-ny).setColor(color);
    }
}
