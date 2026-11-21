package check;
import java.nio.file.*;
import java.util.*;
import java.lang.reflect.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.fastersprint.*;
import net.minecraft.client.*;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.*;
public final class FeatureCheck {
 static boolean connecting,ready;static int warm=80,t,stage=-1,corrections;static boolean air;static int airTicks;static double minY,peak;static int normalTicks;static final List<String> results=new ArrayList<>();
 static final String[] cases={"normal","quick","jump","slab","climb","freecam"};
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 static Minecraft g(){return Minecraft.getInstance();}
 static void command(String s){g().player.connection.sendCommand(s);}
 static void stop(){var o=g().options;for(var k:new KeyMapping[]{o.keyUp,o.keyDown,o.keyLeft,o.keyRight,o.keyJump,o.keyShift})k.setDown(false);}
 public static void correction(ClientboundPlayerPositionPacket p){if(stage>=0&&t>20)corrections++;}
 static Object field(Object o,String name)throws Exception{var f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);}
 static Object bedMesh()throws Exception{var f=BedEsp.class.getDeclaredField("mesh");f.setAccessible(true);return f.get(null);}
 static int beds()throws Exception{return (int)field(bedMesh(),"beds");}
 static void shot(String name)throws Exception{Files.createDirectories(Path.of("/tmp/faster160-live/screenshots"));Screenshot.grab(new java.io.File("/tmp/faster160-live"),name,g().gameRenderer.mainRenderTarget(),1,c->{});}
 static void key(int scan,int code){g().keyboardHandler.keyPress(g().getWindow().handle(),InputConstants.PRESS,new KeyEvent(scan,code,0));g().keyboardHandler.keyPress(g().getWindow().handle(),InputConstants.RELEASE,new KeyEvent(scan,code,0));}
 static void click(int x,int y){var g=g();float s=Math.min(1F,Math.min((g.getWindow().getGuiScaledWidth()-20F)/460,(g.getWindow().getGuiScaledHeight()-20F)/410));ClientMenu.click((g.getWindow().getGuiScaledWidth()-460*s)/2+x*s,(g.getWindow().getGuiScaledHeight()-410*s)/2+y*s);}
 static void record(String s){results.add("PASS "+s);System.out.println("FEATURE PASS "+s);}
 static void begin()throws Exception{stop();FreeCam.disable();stage++;t=0;air=false;airTicks=0;minY=0;peak=0;corrections=0;
  if(stage==cases.length){finish();return;}
  String n=cases[stage];BedEspSettings.setEnabled(false);QuickFallSettings.setEnabled(!n.equals("normal"));WallClimbSettings.setEnabled(n.equals("climb"));
  command(n.equals("climb")?"tp BridgeLive 8.5 81 2.69 0 0":n.equals("slab")?"tp BridgeLive 63.5 105 2.5 0 0":"tp BridgeLive 55.5 105 2.5 0 0");System.out.println("FEATURE START "+n);
 }
 public static void tick(){var g=g();
  if(g.player==null||g.level==null){if(!connecting&&g.gui.overlay()==null){connecting=true;net.minecraft.client.gui.screens.ConnectScreen.startConnecting(g.gui.screen(),g,new net.minecraft.client.multiplayer.resolver.ServerAddress("127.0.0.1",25568),new net.minecraft.client.multiplayer.ServerData("Feature check","127.0.0.1:25568",net.minecraft.client.multiplayer.ServerData.Type.OTHER),false,null);}return;}
  try{
   if(!g.player.isAlive()){g.player.respawn();ready=false;warm=80;return;}
   g.options.pauseOnLostFocus=false;g.gui.toastManager().clear();
   if(!ready){ready=true;g.options.guiScale().set(2);g.options.autoJump().set(false);PlayerDisplaySettings.setAutoBridgeEnabled(false);PlayerDisplaySettings.setAutoHitEnabled(false);PlayerDisplaySettings.setEnabled(false);XraySettings.setEnabled(false);WallClimbSettings.setEnabled(false);QuickFallSettings.setEnabled(false);BedEspSettings.setEnabled(false);command("gamemode survival BridgeLive");command("effect give BridgeLive resistance infinite 4 true");command("tp BridgeLive 8.5 81 0.5 0 10");command("forceload add -16 -32 96 48");}
   if(warm-->0)return;
   t++;
   if(stage==-1){
    if(t==1){command("fill -8 80 -28 20 80 2 stone");command("fill -8 81 -27 20 90 1 air");command("fill -8 81 -15 10 85 -15 stone");command("fill 48 80 -8 72 80 30 stone");command("fill 48 81 -8 72 110 22 air");command("fill 52 104 0 58 104 2 stone");command("fill 60 104 0 66 104 2 stone");command("fill 60 81 3 66 81 20 smooth_stone_slab[type=bottom]");}
    if(t==2)command("tp BridgeLive 0.5 81 -20.5 0 10");
    if(t>=10&&t<42){int i=(t-10)/2;String color=DyeColor.byId(i).getName();int x=-4+(i%4)*3,z=-10+(i/4)*3;boolean head=(t%2)==1;command("setblock "+x+" 81 "+(z+(head?1:0))+" "+color+"_bed[facing=south,part="+(head?"head":"foot")+"]");}
    if(t==43){command("setblock 15 81 -6 red_bed[facing=east,part=foot]");command("setblock 16 81 -6 red_bed[facing=east,part=head]");}
    if(t==60){BedEspSettings.setRange(200);BedEspSettings.setEnabled(true);}
    if(t==180){int count=beds();check(count>=16,"missing beds "+count);Set<Integer> colors=new HashSet<>();for(long edge:(long[])field(bedMesh(),"edges"))colors.add((int)(edge>>>44)&15);check(colors.size()==16,"missing dye colors "+colors);record("all 16 bed colors outlined through a solid wall; logical beds="+count);shot("bed-colors.png");BedEspSettings.setRange(1);}
    if(t==190){check(beds()==0,"range1 retained distant beds");BedEspSettings.setRange(5000);}
    if(t==210){check(beds()>=16,"range5000 failed");record("range 1 hides beds; range 5000 retains loaded beds");command("setblock -4 81 -10 air");command("setblock -4 81 -9 air");}
    if(t==225){check(beds()==16,"bed removal did not update cache: "+beds());record("live server bed removal updates cached outlines");BedEspSettings.setRange(200);key(InputConstants.KEY_P,'p');click(40,210);}
    if(t==230)shot("bedesp-settings.png");
    if(t==235){click(40,90);ClientMenu.scroll(-1);}
    if(t==240)shot("movement-quickfall.png");
    if(t==245){key(InputConstants.KEY_ESCAPE,0);begin();}
    return;
   }
   if(stage>=cases.length)return;String n=cases[stage];
   if(t==20){check(g.player.isAlive(),"actor dead");if(n.equals("freecam"))FreeCam.enable();g.options.keyUp.setDown(true);if(n.equals("jump"))g.options.keyJump.setDown(true);}
   if(t==21)g.options.keyJump.setDown(false);
   if(t>=20){peak=Math.max(peak,g.player.getY());minY=Math.min(minY,g.player.getDeltaMovement().y);
    if(n.equals("climb")){if(t==65){check(g.player.getY()>85,"QuickFall interrupted wall climbing");record("QuickFall permits Wall Climb; rise="+(g.player.getY()-81));begin();}return;}
    if(n.equals("freecam")){if(t==50){check(g.player.getY()>104.9&&g.player.getZ()<2.6,"QuickFall moved freecam body");record("QuickFall leaves Free Cam body in place");begin();}return;}
    if(!g.player.onGround()){air=true;airTicks++;}
    if(air&&g.player.onGround()){
     check(corrections==0,"server corrections "+n+": "+corrections);
     if(n.equals("normal")){normalTicks=airTicks;check(minY> -8,"disabled module accelerated");}
     if(n.equals("quick")||n.equals("slab")){check(minY<=-8,"did not accelerate "+minY);check(airTicks<normalTicks/2,"drop not faster");}
     if(n.equals("slab"))check(Math.abs(g.player.getY()-81.5)<.001,"passed through slab "+g.player.getY());
     if(n.equals("jump"))check(peak>106&&minY> -8,"normal jump changed: "+peak+" "+minY);
     record(n+" drop: airTicks="+airTicks+", minYVelocity="+minY+", landingY="+g.player.getY()+", peak="+peak+", corrections="+corrections);begin();
    }else if(t>180)throw new AssertionError("did not land "+n+" "+g.player.position());
   }
  }catch(Exception e){throw new RuntimeException(e);}
 }
 static void finish()throws Exception{stop();FreeCam.disable();QuickFallSettings.setEnabled(false);WallClimbSettings.setEnabled(false);BedEspSettings.setEnabled(false);Files.writeString(Path.of("/tmp/faster160-live/results.txt"),String.join("\n",results)+"\n");System.out.println("FEATURE ALL CHECKS COMPLETE");g().stop();}
}
