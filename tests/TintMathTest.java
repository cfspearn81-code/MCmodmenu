package net.fastersprint;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import java.lang.reflect.Proxy;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
public class TintMathTest {
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 public static void main(String[] args)throws Exception{
  check(ColorMath.rgb(0,1,1)==0xFFFF0000,"red");check(ColorMath.rgb(1F/3,1,1)==0xFF00FF00,"green");check(ColorMath.rgb(2F/3,1,1)==0xFF0000FF,"blue");check(ColorMath.rgb(1,1,1)==0xFFFF0000,"hue seam");
  for(int h=0;h<=360;h++){check(ColorMath.rgb(h/360F,0,1)==0xFFFFFFFF,"white");check(ColorMath.rgb(h/360F,1,0)==0xFF000000,"black");}
  check(ColorMath.rgb(.5F,1,.5F)==0xFF008080,"brightness");
  var points=new FloatArrayList();PlayerTint.box(points,new Matrix4f().translation(3,4,5).scale(2),0,0,0,1,2,3);check(points.size()==72,"cube must have twelve edges");
  for(int i=0;i<points.size();i+=3){check(points.getFloat(i)==3||points.getFloat(i)==5,"transformed X");check(points.getFloat(i+1)==4||points.getFloat(i+1)==8,"transformed Y");check(points.getFloat(i+2)==5||points.getFloat(i+2)==11,"transformed Z");}
  int[] vertices={0};VertexConsumer out=(VertexConsumer)Proxy.newProxyInstance(VertexConsumer.class.getClassLoader(),new Class<?>[]{VertexConsumer.class},(proxy,m,a)->{
   if(m.getName().equals("addVertex")){vertices[0]++;for(var v:a)check(Float.isFinite(((Number)v).floatValue()),"finite edges");}
   if(m.getName().equals("setColor"))check((int)a[0]==0xFF00FFFF,"chosen color");
   if(m.getName().equals("setNormal")){float x=(float)a[0],y=(float)a[1],z=(float)a[2];check(Math.abs(x*x+y*y+z*z-1)<.00001,"normalized line direction");}
   return proxy;
  });PlayerTint.emit(points.toFloatArray(),0xFF00FFFF,out);check(vertices[0]==24,"line endpoint count");
  var f=GuiColorMesh.class.getDeclaredField("WHEEL_XY");f.setAccessible(true);float[] xy=(float[])f.get(null);check(xy.length==12288,"cached wheel mesh");for(float v:xy)check(Float.isFinite(v)&&v>=0&&v<=1,"wheel bounds");
  System.out.println("PASS HSV full-spectrum/gray/brightness, transformed model edges, finite vertices and cached wheel geometry");
 }
}
