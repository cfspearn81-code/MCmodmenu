package net.fastersprint;
import java.lang.reflect.Proxy;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
public class BedMeshTest {
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 public static void main(String[] args){
  var edges=new LongOpenHashSet();BedMesh.addBox(edges,1,1,-5,1,2,15);check(edges.size()==12,"bed should be one cuboid, not two cubes");
  boolean top=false,longEdge=false;for(long edge:edges){check(BedMesh.color(edge)==15,"color bits");check(OreMesh.y(edge)==1,"base Y");check(BedMesh.y(edge)==1||Math.abs(BedMesh.y(edge)-1-BedGeometry.HEIGHT)<1e-6,"bed height");top|=(edge&BedMesh.TOP)!=0;longEdge|=BedMesh.length(edge)==2;}
  check(top&&longEdge,"missing top/long edges");
  int[] vertices={0};VertexConsumer consumer=(VertexConsumer)Proxy.newProxyInstance(VertexConsumer.class.getClassLoader(),new Class<?>[]{VertexConsumer.class},(proxy,method,params)->{if(method.getName().equals("addVertexWith2DPose")){float x=(float)params[1],y=(float)params[2];check(Float.isFinite(x)&&Float.isFinite(y)&&x>=-.51&&x<=100.51&&y>=-.51&&y<=100.51,"clipped vertex bounds");vertices[0]++;}return proxy;});
  new BedOutlineState(new BedMesh(0,0,0,edges.toLongArray(),1),new Matrix4f().perspective((float)Math.toRadians(90),1,.05F,100),Vec3.ZERO,new Matrix3x2f(),100,100).buildVertices(consumer);check(vertices[0]==48,"twelve line quads");
  edges.clear();BedMesh.addBox(edges,0,0,0,1,2,0);BedMesh.addBox(edges,1,0,0,1,2,0);check(edges.size()==20,"shared edges not deduplicated");
  for(int color=0;color<16;color++)for(int coordinate:new int[]{-5002,5002}){edges.clear();BedMesh.addBox(edges,coordinate,coordinate,coordinate,2,1,color);for(long edge:edges)check(BedMesh.color(edge)==color&&Math.abs(OreMesh.x(edge)-coordinate)<=2&&OreMesh.y(edge)==coordinate,"packed extremes");}
  var section=new OreSection();for(int i=0;i<4096;i++)section.add(i,i%128);for(int i=0;i<4096;i++)check(section.typeAt(i)==i%128,"dense metadata lookup");section.update(7,-1);check(section.typeAt(7)==-1,"removal lookup");
  System.out.println("PASS bed-height combined geometry, palette bits, shared edges, clipping, extreme coordinates and dense lookup");
 }
}
