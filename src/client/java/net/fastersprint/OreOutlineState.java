package net.fastersprint;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.joml.Matrix4f;

/** One GUI element for the entire overlay; no matrix stacks/objects per line. */
final class OreOutlineState implements GuiElementRenderState {
    private static final OreType[] TYPES = OreType.values();
    private final OreMesh mesh;
    private final Matrix4f matrix;
    private final Matrix3x2fc pose;
    private final float originX, originY, originZ, halfWidth, halfHeight;
    private final ScreenRectangle bounds;

    OreOutlineState(OreMesh mesh,Matrix4f matrix,Vec3 camera,Matrix3x2fc pose,int width,int height) {
        this.mesh=mesh;this.matrix=new Matrix4f(matrix);this.pose=new Matrix3x2f(pose);
        originX=(float)(mesh.x-camera.x);originY=(float)(mesh.y-camera.y);originZ=(float)(mesh.z-camera.z);
        halfWidth=width*.5F;halfHeight=height*.5F;
        bounds=new ScreenRectangle(0,0,width,height).transformMaxBounds(pose);
    }
    @Override public RenderPipeline pipeline() { return RenderPipelines.GUI; }
    @Override public TextureSetup textureSetup() { return TextureSetup.noTexture(); }
    @Override public ScreenRectangle scissorArea() { return null; }
    @Override public ScreenRectangle bounds() { return bounds; }
    @Override public void buildVertices(VertexConsumer out) {
        Matrix4f m=matrix;
        for (long edge:mesh.edges) {
            float x=originX+OreMesh.x(edge),y=originY+OreMesh.y(edge),z=originZ+OreMesh.z(edge);
            float ax=m.m00()*x+m.m10()*y+m.m20()*z+m.m30();
            float ay=m.m01()*x+m.m11()*y+m.m21()*z+m.m31();
            float aw=m.m03()*x+m.m13()*y+m.m23()*z+m.m33();
            int axis=OreMesh.axis(edge);
            float bx=ax+(axis==0?m.m00():axis==1?m.m10():m.m20());
            float by=ay+(axis==0?m.m01():axis==1?m.m11():m.m21());
            float bw=aw+(axis==0?m.m03():axis==1?m.m13():m.m23());
            OutlineLine.emit(out,pose,ax,ay,aw,bx,by,bw,halfWidth,halfHeight,TYPES[OreMesh.type(edge)].color);
        }
    }
}
