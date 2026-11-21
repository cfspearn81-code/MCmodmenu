package check;
import java.nio.file.*;
import java.util.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.fastersprint.*;
import net.minecraft.client.*;
import net.minecraft.client.input.*;
import net.minecraft.client.gui.screens.PauseScreen;
public final class TintCheck {
 static boolean connecting,ready;static int warm=100,t;public static int outlines,vertices,outlineColor;public static org.joml.Vector3fc sky;
 static final List<String> results=new ArrayList<>();
 static Minecraft g(){return Minecraft.getInstance();}
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 static void pass(String m){results.add("PASS "+m);System.out.println("TINT PASS "+m);}
 static Object field(String n)throws Exception{var f=ClientMenu.class.getDeclaredField(n);f.setAccessible(true);return f.get(null);}
 static void key(int scan,int code){var g=g();g.keyboardHandler.keyPress(g.getWindow().handle(),1,new KeyEvent(scan,code,0));g.keyboardHandler.keyPress(g.getWindow().handle(),0,new KeyEvent(scan,code,0));}
 static float scale(){var w=g().getWindow();return Math.min(1F,Math.min((w.getGuiScaledWidth()-20F)/460,(w.getGuiScaledHeight()-20F)/410));}
 static double gx(int x){var w=g().getWindow();float s=scale();return(w.getGuiScaledWidth()-460*s)/2+x*s;}
 static double gy(int y){var w=g().getWindow();float s=scale();return(w.getGuiScaledHeight()-410*s)/2+y*s;}
 static void click(int x,int y){ClientMenu.click(gx(x),gy(y));}
 static void drag(int x,int y){ClientMenu.dragPointer(gx(x),gy(y));}
 static void move(int x,int y){var g=g();var w=g.getWindow();g.mouseHandler.onMove(w.handle(),gx(x)*w.getScreenWidth()/w.getGuiScaledWidth(),gy(y)*w.getScreenHeight()/w.getGuiScaledHeight(),0,0);}
 static void press(int x,int y){move(x,y);g().mouseHandler.onButton(g().getWindow().handle(),new MouseButtonInfo(1,0),1);}
 static void release(){g().mouseHandler.onButton(g().getWindow().handle(),new MouseButtonInfo(1,0),0);}
 static void shot(String n)throws Exception{Files.createDirectories(Path.of("/tmp/faster180-live/screenshots"));Screenshot.grab(new java.io.File("/tmp/faster180-live"),n,g().gameRenderer.mainRenderTarget(),1,c->{});}
 static void command(String s){g().player.connection.sendCommand(s);}
 public static void tick(){var g=g();
  if(g.player==null||g.level==null){if(!connecting&&g.gui.overlay()==null){connecting=true;net.minecraft.client.gui.screens.ConnectScreen.startConnecting(g.gui.screen(),g,new net.minecraft.client.multiplayer.resolver.ServerAddress("127.0.0.1",25568),new net.minecraft.client.multiplayer.ServerData("Tint verification","127.0.0.1:25568",net.minecraft.client.multiplayer.ServerData.Type.OTHER),false,null);}return;}
  try{
   g.options.pauseOnLostFocus=false;g.gui.toastManager().clear();
   if(!ready){ready=true;g.options.guiScale().set(2);g.options.renderDistance().set(3);g.options.menuBackgroundBlurriness().set(5);g.options.setCameraType(CameraType.FIRST_PERSON);PlayerDisplaySettings.setAutoBridgeEnabled(false);PlayerDisplaySettings.setAutoHitEnabled(false);PlayerDisplaySettings.setEnabled(false);PlayerDisplaySettings.setGuiMoveEnabled(false);XraySettings.setEnabled(false);BedEspSettings.setEnabled(false);QuickJumpSettings.setEnabled(false);QuickFallSettings.setEnabled(false);WallClimbSettings.setEnabled(false);FreeCam.disable();TintSettings.PLAYER.setEnabled(false);TintSettings.SKY.setEnabled(false);command("gamemode survival BridgeLive");command("tp BridgeLive 0.5 81 -20.5 0 -30");command("forceload add -80 -80 -48 -48");command("time set 6000");command("weather clear");command("clear BridgeLive");}
   if(warm-->0||g.gui.overlay()!=null)return;t++;
   if(t==1){command("fill -70 80 -70 -58 80 -58 stone");command("fill -70 81 -70 -58 96 -58 air");}
   if(t==10)command("tp BridgeLive -64.5 81 -64.5 180 -30");
   if(t==30){key(InputConstants.KEY_P,'p');click(40,120);check((int)field("category")==2,"Visuals category");ClientMenu.scroll(-1);}
   if(t==45)shot("visuals-all.png");
   if(t==50){click(366,65);check((int)field("visualPage")==4,"Tint tab");press(270,244);move(214,308);release();check(Math.abs(TintSettings.PLAYER.hue()-.25)<.02&&TintSettings.PLAYER.saturation()>.99,"color wheel drag");press(367,295);release();check(Math.abs(TintSettings.PLAYER.value()-.5)<.02,"brightness control");click(180,101);check(TintSettings.PLAYER.enabled(),"Player Tint on");pass("wheel drag, brightness and Player Tint switch work");}
   if(t==70)shot("player-color-wheel.png");
   if(t==75){click(424,65);check((int)field("visualPage")==5,"Sky tab");press(282,207);move(360,207);release();check(Math.abs(TintSettings.SKY.hue()-.75)<.02,"hue slider drag");press(282,259);release();check(Math.abs(TintSettings.SKY.saturation()-.5)<.02,"saturation slider");press(360,311);release();check(Math.abs(TintSettings.SKY.value()-.75)<.02,"sky brightness");click(180,101);check(TintSettings.SKY.enabled(),"Sky Tint on");pass("Sky hue/saturation/brightness sliders and switch work");}
   if(t==95)shot("sky-color-sliders.png");
   if(t==100){click(366,65);press(278,244);move(214,180);g.gui.setScreen(new PauseScreen(true));check(!ClientMenu.isOpen(),"pause did not close color drag");check(!g.mouseHandler.isMouseGrabbed(),"pause mouse grabbed");pass("screen transition during a drag commits safely without double blur");}
   if(t==110){g.gui.setScreen(null);TintSettings.SKY.setEnabled(false);TintSettings.PLAYER.preview(.5F,1,1);TintSettings.PLAYER.setEnabled(true);outlines=0;}
   if(t==130){check(outlines>0&&vertices>=24&&outlineColor==0xFF00FFFF,"first person hand outline absent "+outlines+" "+vertices+" "+Integer.toHexString(outlineColor));pass("first-person visible hand edges: vertices="+vertices+", color=cyan");shot("hand-cyan.png");g.options.setCameraType(CameraType.THIRD_PERSON_FRONT);outlines=0;}
   if(t==150){check(outlines>0&&vertices>=144,"third-person body incomplete "+vertices);pass("F5 front outlines complete animated model: vertices="+vertices);shot("body-front-cyan.png");g.options.setCameraType(CameraType.THIRD_PERSON_BACK);TintSettings.PLAYER.preview(0,1,1);TintSettings.PLAYER.save();outlines=0;}
   if(t==170){check(outlines>0&&outlineColor==0xFFFF0000,"back body/new color absent");pass("F5 back outline updates to selected red");shot("body-back-red.png");g.options.setCameraType(CameraType.FIRST_PERSON);g.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND,net.minecraft.world.item.component.SwingAnimation.DEFAULT,false);outlines=0;}
   if(t==180){check(outlines>0&&outlineColor==0xFFFF0000,"swing lost outline");pass("hand outline follows swing animation");TintSettings.PLAYER.setEnabled(false);outlines=0;}
   if(t==200){check(outlines==0,"disabled outline persisted");pass("disabling Player Tint stops all edge submissions");shot("hand-off.png");TintSettings.SKY.preview(0,1,1);TintSettings.SKY.setEnabled(true);g.player.setXRot(-45);}
   if(t==220){check(sky!=null&&sky.x()>.99&&sky.y()<.01&&sky.z()<.01,"red sky state missing "+sky);pass("sky render state uses chosen red");shot("sky-red.png");TintSettings.SKY.preview(.5F,1,1);TintSettings.SKY.save();}
   if(t==240){check(sky.x()<.01&&sky.y()>.99&&sky.z()>.99,"cyan sky update missing");pass("sky color updates live to cyan");shot("sky-cyan.png");TintSettings.SKY.setEnabled(false);}
   if(t==260){check(!(sky.x()<.01&&sky.y()>.99&&sky.z()>.99),"sky did not restore");pass("disabling Sky Tint restores natural sky");shot("sky-off.png");finish();}
  }catch(Exception e){throw new RuntimeException(e);}
 }
 static void finish()throws Exception{TintSettings.PLAYER.setEnabled(false);TintSettings.SKY.setEnabled(false);Files.writeString(Path.of("/tmp/faster180-live/results.txt"),String.join("\n",results)+"\n");System.out.println("TINT ALL CHECKS COMPLETE");g().stop();}
}
