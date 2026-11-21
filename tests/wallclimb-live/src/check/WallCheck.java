package check;

import java.nio.file.*;
import java.lang.reflect.*;
import java.util.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.fastersprint.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.phys.Vec3;

public final class WallCheck {
 static final String[] CASES={"ladder","wall","off","diagonal","corner","ledge","floor","air","freecam","bridgewall"};
 static final List<String> RESULTS=new ArrayList<>();
 static final Map<String,List<Double>> HEIGHTS=new LinkedHashMap<>();
 public static boolean holdBridge;
 static boolean connecting,ready,started;static int warmup=60,stage=-1,ticks,corrections;static Vec3 start;static double held,slide,hang;
 static List<Double> trace;
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 static void stop(){var g=Minecraft.getInstance();for(var k:new net.minecraft.client.KeyMapping[]{g.options.keyUp,g.options.keyDown,g.options.keyLeft,g.options.keyRight,g.options.keyJump,g.options.keyShift,g.options.keySprint})k.setDown(false);}
 static void key(int scan,int code,int action){var g=Minecraft.getInstance();g.keyboardHandler.keyPress(g.getWindow().handle(),action,new KeyEvent(scan,code,0));}
 static void text(String s){for(char c:s.toCharArray()){int scan=InputConstants.KEY_A+c-'a';key(scan,c,InputConstants.PRESS);key(scan,c,InputConstants.RELEASE);}}
 static void click(int x,int y){var g=Minecraft.getInstance();float scale=Math.min(1F,Math.min((g.getWindow().getGuiScaledWidth()-20F)/460,(g.getWindow().getGuiScaledHeight()-20F)/410));ClientMenu.click((g.getWindow().getGuiScaledWidth()-460*scale)/2+x*scale,(g.getWindow().getGuiScaledHeight()-410*scale)/2+y*scale);}
 static Object menu(String key)throws Exception{var f=ClientMenu.class.getDeclaredField(key);f.setAccessible(true);return f.get(null);}
 public static void correction(ClientboundPlayerPositionPacket packet){if(stage>=0&&stage<CASES.length&&ticks>20)corrections++;}
 static void begin()throws Exception{
  var g=Minecraft.getInstance();stop();FreeCam.disable();stage++;ticks=0;corrections=0;
  if(stage>=CASES.length){finish();return;}
  String name=CASES[stage];trace=new ArrayList<>();holdBridge=name.equals("bridgewall");PlayerDisplaySettings.setAutoBridgeEnabled(holdBridge);WallClimbSettings.setEnabled(!name.equals("ladder")&&!name.equals("off"));
  double x=switch(name){case "ladder"->.5;case "diagonal"->12.5;case "corner"->24.69;case "ledge"->34.5;default->8.5;};
  double y=name.equals("air")?110:81,z=(name.equals("floor")||name.equals("air"))?-5.5:name.equals("ladder")?2.5:2.69;
  float yaw=(name.equals("diagonal")||name.equals("corner"))?-45:0;
  g.player.connection.sendCommand("effect give BridgeLive instant_health 1 5 true");
  g.player.connection.sendCommand("tp BridgeLive "+x+" "+y+" "+z+" "+yaw+" 0");
  System.out.println("WALL CLIMB START "+name);
 }
 public static void tick(){var g=Minecraft.getInstance();
  if(g.player==null||g.level==null){if(!connecting&&g.gui.overlay()==null){connecting=true;net.minecraft.client.gui.screens.ConnectScreen.startConnecting(g.gui.screen(),g,new net.minecraft.client.multiplayer.resolver.ServerAddress("127.0.0.1",25568),new net.minecraft.client.multiplayer.ServerData("Wall Climb check","127.0.0.1:25568",net.minecraft.client.multiplayer.ServerData.Type.OTHER),false,null);}return;}
  try{
   g.options.pauseOnLostFocus=false;g.gui.toastManager().clear();
   if(!ready){ready=true;g.options.guiScale().set(2);g.options.autoJump().set(false);PlayerDisplaySettings.setAutoBridgeEnabled(false);PlayerDisplaySettings.setAutoHitEnabled(false);PlayerDisplaySettings.setEnabled(false);XraySettings.setEnabled(false);WallClimbSettings.setEnabled(false);g.player.connection.sendCommand("gamemode survival BridgeLive");g.player.connection.sendCommand("tp BridgeLive 8.5 81 0.5 0 0");}
   if(warmup-->0)return;
   if(!started){started=true;key(InputConstants.KEY_P,'p',InputConstants.PRESS);text("wallclimb");check(ClientMenu.isOpen()&&menu("query").equals("wallclimb"),"module search failed");var m=ClientMenu.class.getDeclaredMethod("searchResults");m.setAccessible(true);check(((List<?>)m.invoke(null)).size()==1,"new module missing/duplicated");return;}
   if(stage==-1){ticks++;if(ticks==5){shot("wall-climb-search.png");click(180,109);check(WallClimbSettings.enabled(),"menu toggle on failed");click(180,109);check(!WallClimbSettings.enabled(),"menu toggle off failed");click(40,90);}if(ticks==10)shot("movement-modules.png");if(ticks==15){key(InputConstants.KEY_ESCAPE,0,InputConstants.PRESS);begin();}return;}
   if(stage>=CASES.length)return;
   String name=CASES[stage];ticks++;
   if(ticks==20){
    start=g.player.position();check(g.player.isAlive(),"test actor died");
    if(name.equals("ladder"))check(g.player.onClimbable(),"reference ladder missing");
    if(name.equals("floor")||name.equals("air"))check(!WallClimb.onWall(g.player),"floor/air treated as wall");
    if(name.equals("off"))check(!g.player.onClimbable(),"disabled module treated stone as ladder");
    if(name.equals("freecam")){FreeCam.enable();check(!WallClimb.onWall(g.player),"climb active during freecam");}
    if(!name.equals("floor")&&!name.equals("air"))g.options.keyUp.setDown(true);
   }
   if(ticks>=20){trace.add(g.player.getY()-start.y);
    if(ticks%20==0)System.out.println("WALL SAMPLE "+name+" t="+ticks+" pos="+g.player.position()+" velocity="+g.player.getDeltaMovement()+" input="+g.player.input.keyPresses+" horizontalCollision="+g.player.horizontalCollision);
    if(name.equals("ladder")||name.equals("wall")){
     if(ticks==140){g.options.keyUp.setDown(false);held=g.player.getY();check(held-start.y>12,"climb did not progress: "+(held-start.y));}
     if(ticks==160){slide=g.player.getY();check(held-slide>1&&held-slide<3.5,"not controlled ladder sliding");g.options.keyShift.setDown(true);}
     if(ticks==180){hang=g.player.getY();check(Math.abs(hang-slide)<.17,"sneak did not hold position");g.options.keyShift.setDown(false);g.options.keyDown.setDown(true);}
     if(name.equals("wall")&&ticks==80)shot("wall-climb-active.png");
     if(ticks==200){stop();check(!g.player.onClimbable(),"retreat did not leave wall/ladder");HEIGHTS.put(name,trace);record(name,"rise="+(held-start.y)+", slide="+(held-slide)+", holdDelta="+(hang-slide));begin();}
    }else if(name.equals("ledge")){
     if(g.player.getY()>=84.99&&g.player.getZ()>3.25){stop();record(name,"topped out y="+g.player.getY()+", z="+g.player.getZ());begin();}
     else if(ticks>110)throw new AssertionError("could not walk onto ledge");
    }else if(name.equals("diagonal")||name.equals("corner")||name.equals("bridgewall")){
     if(ticks==65){check(g.player.getY()-start.y>4,"angled/corner climbing failed");record(name,"rise="+(g.player.getY()-start.y));begin();}
    }else if(name.equals("off")||name.equals("floor")){
     if(ticks==60){check(Math.abs(g.player.getY()-start.y)<.05,"unexpected climb while off/on flat floor");record(name,"rise="+(g.player.getY()-start.y));begin();}
    }else if(name.equals("air")){
     if(ticks==23){check(g.player.getDeltaMovement().y<-.5,"off-wall gravity was clamped");record(name,"fallVelocity="+g.player.getDeltaMovement().y);begin();}
    }else if(name.equals("freecam")){
     if(ticks==50){check(g.player.position().distanceTo(start)<.04,"camera controls climbed the player");check(FreeCam.position(1).distanceTo(g.player.getEyePosition())>10,"camera did not move");record(name,"bodyMovement="+g.player.position().distanceTo(start));begin();}
    }
   }
  }catch(Exception e){throw new RuntimeException(e);}
 }
 static void record(String name,String values){check(corrections==0,"server corrected "+name+" "+corrections+" times");String line="PASS "+name+": "+values+", unexpectedServerCorrections="+corrections;RESULTS.add(line);System.out.println(line);}
 static void finish()throws Exception{
  stop();FreeCam.disable();WallClimbSettings.setEnabled(false);PlayerDisplaySettings.setAutoBridgeEnabled(false);holdBridge=false;
  var a=HEIGHTS.get("ladder");var b=HEIGHTS.get("wall");double max=0;
  for(int i=0;i<160;i++)max=Math.max(max,Math.abs(a.get(i)-b.get(i)));
  check(max<.00001,"wall path differs from vanilla ladder: "+max);
  RESULTS.add("PASS wall trajectory matches real ladder through climb, slide and sneak: maxDelta="+max);
  Files.writeString(Path.of("/tmp/wallclimb-live/results.txt"),String.join("\n",RESULTS)+"\n");
  Files.writeString(Path.of("/tmp/wallclimb-live/trajectories.json"),"{\"ladder\":"+a+",\"wall\":"+b+"}");
  System.out.println("WALL CLIMB ALL CHECKS COMPLETE maxLadderDelta="+max);Minecraft.getInstance().stop();
 }
 static void shot(String name)throws Exception{Files.createDirectories(Path.of("/tmp/wallclimb-preview/screenshots"));var g=Minecraft.getInstance();Screenshot.grab(new java.io.File("/tmp/wallclimb-preview"),name,g.gameRenderer.mainRenderTarget(),1,c->System.out.println("WALL SCREENSHOT "+c.getString()));}
}
