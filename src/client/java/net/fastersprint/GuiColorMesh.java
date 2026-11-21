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

/** A whole color wheel/gradient is one GUI element, with cached wheel vertices. */
final class GuiColorMesh implements GuiElementRenderState {
    private static final int SECTORS=96,RINGS=16;
    private static final float[] WHEEL_XY=new float[SECTORS*RINGS*8];
    private static final int[] WHEEL_COLORS=new int[SECTORS*RINGS*4];
    static {
        int n=0;
        for(int ring=0;ring<RINGS;ring++)for(int sector=0;sector<SECTORS;sector++)for(int corner=0;corner<4;corner++){
            float h=(sector+((corner==1||corner==2)?1:0))/(float)SECTORS;
            float s=(ring+(corner>=2?1:0))/(float)RINGS;
            WHEEL_XY[n*2]=.5F+.5F*s*(float)Math.cos(h*Math.PI*2);WHEEL_XY[n*2+1]=.5F+.5F*s*(float)Math.sin(h*Math.PI*2);
            WHEEL_COLORS[n++]=ColorMath.rgb(h,s,1);
        }
    }
    private final float[] xy;private final int[] colors;private final Matrix3x2f pose;private final ScreenRectangle bounds;
    private GuiColorMesh(GuiGraphicsExtractor g,float x,float y,float w,float h,float[] xy,int[] colors){
        this.xy=xy;this.colors=colors;pose=new Matrix3x2f(g.pose()).translate(x,y).scale(w,h);
        bounds=new ScreenRectangle((int)x,(int)y,(int)Math.ceil(w),(int)Math.ceil(h)).transformMaxBounds(g.pose());
    }
    static void wheel(GuiGraphicsExtractor g,int x,int y,int size){submit(g,new GuiColorMesh(g,x,y,size,size,WHEEL_XY,WHEEL_COLORS));}
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
