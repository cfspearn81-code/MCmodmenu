package net.fastersprint;

import java.util.Optional;
import com.mojang.blaze3d.vertex.*;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import net.fastersprint.mixin.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.*;
import org.joml.Matrix4f;

/** Snapshot animated model edges once per submission, then draw one batch for hands/body. */
public final class PlayerTint {
    private static final PoseStack IDENTITY=new PoseStack();
    private PlayerTint() { }
    private static final class Pipeline {
        static final RenderType LINES=TintRenderTypeInvoker.fastersprint$create("faster_player_tint",
            RenderSetup.builder(TintPipelinesAccessor.fastersprint$register(RenderPipeline.builder(TintPipelinesAccessor.fastersprint$lines())
                .withLocation(net.minecraft.resources.Identifier.fromNamespaceAndPath("fastersprint","pipeline/player_tint")).withColorTargetState(net.minecraft.client.renderer.RenderPipelines.LINES.getColorTargetStates().get(0))
                .withDepthStencilState(Optional.empty()).build())).createRenderSetup());
    }
    public static boolean active(){var g=Minecraft.getInstance();return TintSettings.PLAYER.enabled()&&g.level!=null&&g.player!=null&&g.player.isAlive()&&!g.player.isSpectator()&&!g.player.isInvisible();}
    public static boolean body(AvatarRenderState state){var g=Minecraft.getInstance();return active()&&state.id==g.player.getId()&&g.gui.screen()==null&&(!g.options.getCameraType().isFirstPerson()||FreeCam.active());}
    public static void hand(ModelPart part,PoseStack pose,SubmitNodeCollector out){if(active()&&!FreeCam.active())submit(part,pose,out);}
    public static void submit(ModelPart root,PoseStack pose,SubmitNodeCollector out){
        FloatArrayList points=new FloatArrayList(288);capture(root,pose,points);
        if(points.isEmpty())return;float[] vertices=points.toFloatArray();int color=TintSettings.PLAYER.color();
        out.order(1000).submitCustomGeometry(IDENTITY,Pipeline.LINES,(ignored,consumer)->emit(vertices,color,consumer));
    }
    private static void capture(ModelPart part,PoseStack pose,FloatArrayList points){
        if(!part.visible)return;
        pose.pushPose();part.translateAndRotate(pose);
        var data=(TintModelPartAccessor)(Object)part;
        if(!part.skipDraw)for(var cube:data.fastersprint$cubes()){
            float ax=Float.POSITIVE_INFINITY,ay=ax,az=ax,bx=Float.NEGATIVE_INFINITY,by=bx,bz=bx;
            for(var polygon:cube.polygons)for(var vertex:polygon.vertices()){
                ax=Math.min(ax,vertex.worldX());ay=Math.min(ay,vertex.worldY());az=Math.min(az,vertex.worldZ());
                bx=Math.max(bx,vertex.worldX());by=Math.max(by,vertex.worldY());bz=Math.max(bz,vertex.worldZ());
            }
            if(Float.isFinite(ax))box(points,pose.last().pose(),ax,ay,az,bx,by,bz);
        }
        for(var child:data.fastersprint$children().values())capture(child,pose,points);
        pose.popPose();
    }
    static void box(FloatArrayList points,Matrix4f matrix,float ax,float ay,float az,float bx,float by,float bz){
        for(int corner=0;corner<8;corner++)for(int axis=0;axis<3;axis++)if((corner&(1<<axis))==0){
            point(points,matrix,(corner&1)==0?ax:bx,(corner&2)==0?ay:by,(corner&4)==0?az:bz);
            int end=corner|(1<<axis);point(points,matrix,(end&1)==0?ax:bx,(end&2)==0?ay:by,(end&4)==0?az:bz);
        }
    }
    private static void point(FloatArrayList out,Matrix4f m,float x,float y,float z){out.add(m.m00()*x+m.m10()*y+m.m20()*z+m.m30());out.add(m.m01()*x+m.m11()*y+m.m21()*z+m.m31());out.add(m.m02()*x+m.m12()*y+m.m22()*z+m.m32());}
    static void emit(float[] positions,int color,VertexConsumer out){
        for(int i=0;i<positions.length;i+=6){float dx=positions[i+3]-positions[i],dy=positions[i+4]-positions[i+1],dz=positions[i+5]-positions[i+2];float length=(float)Math.sqrt(dx*dx+dy*dy+dz*dz);if(length<.000001F)continue;dx/=length;dy/=length;dz/=length;
            out.addVertex(positions[i],positions[i+1],positions[i+2]).setColor(color).setNormal(dx,dy,dz).setLineWidth(1.5F);
            out.addVertex(positions[i+3],positions[i+4],positions[i+5]).setColor(color).setNormal(dx,dy,dz).setLineWidth(1.5F);
        }
    }
}
