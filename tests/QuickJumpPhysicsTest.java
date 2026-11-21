package net.fastersprint;
public class QuickJumpPhysicsTest {
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 public static void main(String[] args){
  double normal=QuickJumpPhysics.rise(.42F,.08,.98F);
  check(Math.abs(normal-1.252203340253729)<1e-12,"normal jump reference changed: "+normal);
  double boosted=QuickJumpPhysics.rise(.52F,.08,.98F),honey=QuickJumpPhysics.rise(.21F,.08,.98F);
  check(boosted>normal&&boosted<2,"Jump Boost height");check(honey>0&&honey<normal/2,"reduced surface height");
  for(double bad:new double[]{Double.NaN,Double.POSITIVE_INFINITY,0,-1})check(QuickJumpPhysics.rise(.42,bad,.98)==0,"unsupported gravity");
  check(QuickJumpPhysics.rise(Double.NaN,.08,.98)==0,"bad impulse");check(QuickJumpPhysics.rise(.42,.08,1.01)==0,"amplified drag");
  check(QuickJumpPhysics.rise(10,.08,.98)==0,"extreme height fallback");check(QuickJumpPhysics.rise(.42,1e-20,1)==0,"bounded low-gravity calculation");
  check(QuickJumpPhysics.rise(.0001,.08,.98)==.0001,"first movement must precede the small-velocity cutoff");
  System.out.println("PASS exact vanilla jump peak, boost/surface height, unsupported physics and bounded work");
 }
}
