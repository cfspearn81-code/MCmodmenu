package net.fastersprint;
public class FreeCamMotionSearchTest {
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 static void near(double a,double b){check(Math.abs(a-b)<1e-6,a+" != "+b);}
 public static void main(String[] args){
  near(FreeCamMotion.step(0,0,1,0,0,false).z,.6);
  near(FreeCamMotion.step(0,0,0,1,0,false).x,-.6);
  near(FreeCamMotion.step(90,0,1,0,0,false).x,-.6);
  near(FreeCamMotion.step(0,-90,1,0,0,false).y,.6);
  near(FreeCamMotion.step(0,0,0,0,-1,false).y,-.6);
  near(FreeCamMotion.step(35,-20,1,1,1,false).length(),.6);
  near(FreeCamMotion.step(35,-20,1,1,1,true).length(),1.8);
  near(FreeCamMotion.step(35,-20,0,0,0,true).length(),0);
  check(ModuleSearch.matches("Free Cam","Fly the camera freely","freecam"),"compact name");
  check(ModuleSearch.matches("Free Cam","Fly the camera freely"," CAM  FREE "),"tokens/case");
  check(ModuleSearch.matches("GuiMove","Move with any screen open","gui move"),"spaced camel name");
  check(ModuleSearch.matches("Player ESP","Boxes, tracers + distances","ESP"),"ESP alias");
  check(ModuleSearch.matches("Xray","Ores","  "),"empty query");
  check(!ModuleSearch.matches("Auto Hit","Cooldown","bridge"),"irrelevant match");
  check(ModuleSearch.backspace("a\ud83d\ude00").equals("a"),"surrogate backspace");
  check(ModuleSearch.append("a","\n\tb").equals("ab"),"control characters");
  check(ModuleSearch.append("x".repeat(63),"yz").length()==64,"input bound");
  System.out.println("PASS Free Cam movement, normalized diagonals, speed, module search and text editing");
 }
}
