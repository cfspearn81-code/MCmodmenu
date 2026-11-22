package net.fastersprint;
public class AimAnglesTest {
 static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
 public static void main(String[] args){
  for(int a=0;a<360;a+=5)for(int height=-3;height<=3;height++){
   double x=8*Math.cos(Math.toRadians(a)),z=8*Math.sin(Math.toRadians(a)),y=height*2,length=Math.sqrt(x*x+y*y+z*z);
   var look=AimAngles.toward(x,y,z,725);double yaw=Math.toRadians(look.yaw()),pitch=Math.toRadians(look.pitch());
   double dot=(-Math.sin(yaw)*Math.cos(pitch)*x-Math.sin(pitch)*y+Math.cos(yaw)*Math.cos(pitch)*z)/length;
   check(dot>.999999,"head direction "+a+" "+height);check(Math.abs(look.yaw()-725)<=180.001,"long yaw route");
  }
  var up=AimAngles.toward(0,2,0,123);check(up.yaw()==123&&up.pitch()==-90,"vertical target");
  var down=AimAngles.toward(0,-2,0,-41);check(down.yaw()==-41&&down.pitch()==90,"lower target");
  var zero=AimAngles.toward(0,0,0,30);check(Float.isFinite(zero.yaw())&&Float.isFinite(zero.pitch()),"coincident eyes");
  System.out.println("PASS 504 head-direction/shortest-yaw cases, vertical targets and finite coincident inputs");
 }
}
