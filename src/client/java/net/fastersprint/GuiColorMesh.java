package net.fastersprint;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.fastersprint.mixin.GuiRenderStateAccessor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2f;

/** A whole slider gradient is one GUI element. */
final class GuiColorMesh implements GuiElementRenderState {
    private final float[] xy;private final int[] colors;private final Matrix3x2f pose;private final ScreenRectangle bounds;
    private GuiColorMesh(GuiGraphicsExtractor g,float x,float y,float w,float h,float[] xy,int[] colors){
        this.xy=xy;this.colors=colors;pose=new Matrix3x2f(g.pose()).translate(x,y).scale(w,h);
        bounds=new ScreenRectangle((int)x,(int)y,(int)Math.ceil(w),(int)Math.ceil(h)).transformMaxBounds(g.pose());
    }
    static void gradient(GuiGraphicsExtractor g,int x,int y,int w,int h,int... stops){
        int segments=stops.length-1;float[] xy=new float[segments*8];int[] colors=new int[segments*4];
        for(int i=0;i<segments;i++){int p=i*8;float a=i/(float)segments,b=(i+1)/(float)segments;xy[p]=a;xy[p+1]=0;xy[p+2]=a;xy[p+3]=1;xy[p+4]=b;xy[p+5]=1;xy[p+6]=b;xy[p+7]=0;colors[i*4]=colors[i*4+1]=stops[i];colors[i*4+2]=colors[i*4+3]=stops[i+1];}
        submit(g,new GuiColorMesh(g,x,y,w,h,xy,colors));
    }
    private static void submit(GuiGraphicsExtractor g,GuiColorMesh state){((GuiRenderStateAccessor)g).fastersprint$guiRenderState().addGuiElement(state);}
    @Override public RenderPipeline pipeline(){return RenderPipelines.GUI;}
    @Override public TextureSetup textureSetup(){return TextureSetup.noTexture();}
    @Override public ScreenRectangle scissorArea(){return null;}
    @Override public ScreenRectangle bounds(){return bounds;}
    @Override public void buildVertices(VertexConsumer out){for(int i=0;i<colors.length;i++)out.addVertexWith2DPose(pose,xy[i*2],xy[i*2+1]).setColor(colors[i]);}
}
