package check;
import java.nio.file.*;
import java.util.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.fastersprint.*;
import net.minecraft.client.*;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.renderer.fog.FogData;
public final class HorizonCheck {
 static boolean connecting,ready;static int warm=100,t;static float[] distances;static float alpha;
 static final List<String> results=new ArrayList<>();
 static Minecraft g(){return Minecraft.getInstance();}
 static FogData fog(){return g().gameRenderer.gameRenderState().levelRenderState.cameraRenderState.fogData;}
 static float[] ranges(FogData f){return new float[]{f.environmentalStart,f.environmentalEnd,f.renderDistanceStart,f.renderDistanceEnd,f.skyEnd,f.cloudEnd};}
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 static void pass(String m){results.add("PASS "+m);System.out.println("HORIZON PASS "+m);}
 static void command(String s){g().player.connection.sendCommand(s);}
 static void key(int scan,int code){var g=g();g.keyboardHandler.keyPress(g.getWindow().handle(),1,new KeyEvent(scan,code,0));g.keyboardHandler.keyPress(g.getWindow().handle(),0,new KeyEvent(scan,code,0));}
 static Object field(String n)throws Exception{var f=ClientMenu.class.getDeclaredField(n);f.setAccessible(true);return f.get(null);}
 static void click(int x,int y){var w=g().getWindow();float s=Math.min(1F,Math.min((w.getGuiScaledWidth()-20F)/460,(w.getGuiScaledHeight()-20F)/410));ClientMenu.click((w.getGuiScaledWidth()-460*s)/2+x*s,(w.getGuiScaledHeight()-410*s)/2+y*s);}
 static void shot(String name)throws Exception{Files.createDirectories(Path.of("/tmp/faster181-live/screenshots"));Screenshot.grab(new java.io.File("/tmp/faster181-live"),name,g().gameRenderer.mainRenderTarget(),1,c->{});}
 static void tinted(){var selected=TintSettings.SKY.vector();var f=fog();check(Math.abs(f.color.x()-selected.x())<.00001&&Math.abs(f.color.y()-selected.y())<.00001&&Math.abs(f.color.z()-selected.z())<.00001,"horizon background does not match chosen color "+f.color);check(f.color.w()==alpha,"fog alpha changed");check(Arrays.equals(ranges(f),distances),"native fog distances changed");}
 public static void tick(){var g=g();
  if(g.player==null||g.level==null){if(!connecting&&g.gui.overlay()==null){connecting=true;net.minecraft.client.gui.screens.ConnectScreen.startConnecting(g.gui.screen(),g,new net.minecraft.client.multiplayer.resolver.ServerAddress("127.0.0.1",25568),new net.minecraft.client.multiplayer.ServerData("Horizon regression","127.0.0.1:25568",net.minecraft.client.multiplayer.ServerData.Type.OTHER),false,null);}return;}
  try{
   g.options.pauseOnLostFocus=false;g.gui.toastManager().clear();
   if(!ready){ready=true;g.options.guiScale().set(2);g.options.renderDistance().set(3);g.options.setCameraType(CameraType.FIRST_PERSON);PlayerDisplaySettings.setAutoBridgeEnabled(false);PlayerDisplaySettings.setAutoHitEnabled(false);PlayerDisplaySettings.setEnabled(false);PlayerDisplaySettings.setGuiMoveEnabled(false);XraySettings.setEnabled(false);BedEspSettings.setEnabled(false);QuickJumpSettings.setEnabled(false);QuickFallSettings.setEnabled(false);WallClimbSettings.setEnabled(false);FreeCam.disable();TintSettings.SKY.setEnabled(false);command("gamemode survival BridgeLive");command("tp BridgeLive -64.5 81 -64.5 0 0");command("time set 6000");command("weather clear");command("clear BridgeLive");}
   if(warm-->0||g.gui.overlay()!=null)return;t++;
   if(t==1){key(InputConstants.KEY_P,'p');for(char c:"player tint".toCharArray())key(InputConstants.KEY_A,c);var m=ClientMenu.class.getDeclaredMethod("searchResults");m.setAccessible(true);check(((List<?>)m.invoke(null)).isEmpty(),"Player Tint remains searchable");click(40,120);Class<?> tool=Class.forName("net.fastersprint.ClientMenu$Tool");check(tool.getEnumConstants().length==12,"wrong module count");Set<String> names=new HashSet<>();var visible=ClientMenu.class.getDeclaredMethod("visible",tool);visible.setAccessible(true);for(var v:tool.getEnumConstants())if((boolean)visible.invoke(null,v))names.add(v.toString());check(names.equals(Set.of("XRAY","PLAYERS","BEDESP","FREECAM","SKYTINT")),"Visuals layout "+names);pass("Player Tint removed from Search and Visuals; all five remaining visuals fit");}
   if(t==15)shot("visuals-no-player-tint.png");
   if(t==20){click(416,65);check((int)field("visualPage")==4,"Sky settings moved incorrectly");click(282,207);ClientMenu.releasePointer();check(Math.abs(TintSettings.SKY.hue()-.5)<.02,"sky slider broken");}
   if(t==35)shot("sky-settings.png");
   if(t==40){ClientMenu.toggle();}
   if(t==55){distances=ranges(fog());alpha=fog().color.w();shot("horizon-off.png");TintSettings.SKY.preview(5F/6,1,1);TintSettings.SKY.setEnabled(true);}
   if(t==80){tinted();shot("horizon-pink-south.png");g.player.setYRot(90);pass("south horizon fog/background matches magenta; all six fog distances/alpha unchanged");}
   if(t==105){tinted();shot("horizon-pink-west.png");g.player.setYRot(180);}
   if(t==130){tinted();shot("horizon-pink-north.png");g.player.setYRot(270);}
   if(t==155){tinted();shot("horizon-pink-east.png");pass("horizontal views in all four directions use the same selected sky/horizon color");TintSettings.SKY.preview(.5F,1,1);TintSettings.SKY.save();}
   if(t==180){tinted();shot("horizon-cyan.png");pass("horizon changes live to cyan");TintSettings.SKY.setEnabled(false);}
   if(t==205){var f=fog();check(f.color.x()>.1&&f.color.z()>f.color.x(),"native background did not restore "+f.color);check(Arrays.equals(ranges(f),distances),"ranges not restored");shot("horizon-restored.png");pass("disabling restores the native blue horizon and sky");TintSettings.SKY.setEnabled(true);command("fill -62 81 -62 -59 85 -59 water");}
   if(t==220)command("tp BridgeLive -60.5 82 -60.5 0 0");
   if(t==245){check(g.gameRenderer.mainCamera().getFluidInCamera()==net.minecraft.world.level.material.FogType.WATER,"water fixture missing");var selected=TintSettings.SKY.vector();check(fog().color.distance(new org.joml.Vector4f(selected,alpha))>.05,"tint overwrote underwater fog");pass("underwater fog retains its native color while Sky Tint is enabled");command("tp BridgeLive -64.5 81 -64.5 0 0");}
   if(t==265){key(InputConstants.KEY_P,'p');click(40,120);click(416,65);click(300,207);g.gui.setScreen(new PauseScreen(true));check(!ClientMenu.isOpen(),"pause did not dismiss drag");pass("screen transition during sky editing remains safe");g.gui.setScreen(null);TintSettings.SKY.setEnabled(false);Files.writeString(Path.of("/tmp/faster181-live/results.txt"),String.join("\n",results)+"\n");System.out.println("HORIZON ALL CHECKS COMPLETE");g.stop();}
  }catch(Exception e){throw new RuntimeException(e);}
 }
}
