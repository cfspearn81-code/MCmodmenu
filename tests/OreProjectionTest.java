package net.fastersprint;
import java.lang.reflect.Proxy;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import net.minecraft.world.phys.Vec3;
public class OreProjectionTest {
 static int count;
 static void check(boolean v,String message){if(!v)throw new AssertionError(message);}
 static int vertices(int x,int y,int z){
  count=0;var edges=new LongOpenHashSet();OreMesh.addBox(edges,x,y,z,5);
  var projection=new Matrix4f().perspective((float)Math.toRadians(90),1,.05F,100);
  VertexConsumer out=(VertexConsumer)Proxy.newProxyInstance(VertexConsumer.class.getClassLoader(),new Class<?>[]{VertexConsumer.class},(proxy,method,args)->{
   if(method.getName().equals("addVertexWith2DPose")){
    float px=(float)args[1],py=(float)args[2];
    check(Float.isFinite(px)&&Float.isFinite(py),"non-finite clipped vertex");
    check(px>=-.51F&&px<=100.51F&&py>=-.51F&&py<=100.51F,"line escaped viewport");count++;
   }
   return proxy;
  });
  new OreOutlineState(new OreMesh(0,0,0,edges.toLongArray()),projection,Vec3.ZERO,new Matrix3x2f(),100,100).buildVertices(out);
  return count;
 }
 public static void main(String[] args){
  check(vertices(1,1,-5)==48,"front cube must retain all twelve lines");
  check(vertices(0,0,5)==0,"behind camera must not render");
  check(vertices(0,0,-1)>0,"near-plane intersections must remain visible");
  check(vertices(4,0,-5)>0,"viewport boundary clipping");
  var frustum=new OreProjection(new Matrix4f().perspective((float)Math.toRadians(90),1,.05F,100));
  check(frustum.visible(0,0,-500,1),"ore range must not be clipped at terrain far plane");
  check(!frustum.visible(100,0,-5,1)&&!frustum.visible(0,0,5,1),"frustum failed to reject off-screen boxes");
  System.out.println("PASS: batched vertex counts, near/screen clipping, finite bounds and distant ore visibility");
 }
}
