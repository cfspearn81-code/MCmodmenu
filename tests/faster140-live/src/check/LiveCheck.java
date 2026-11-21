package check;

import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.fastersprint.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.client.Screenshot;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import check.mixin.MouseAccess;

public final class LiveCheck {
 public static boolean focused=true,audit;
 static boolean connecting,ready;static int ticks,positionPackets,inputPackets;
 static Vec3 body,start,paused;static float bodyYaw,bodyPitch;
 static final List<String> passes=new ArrayList<>();
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 static void pass(String m){passes.add(m);System.out.println("PASS "+m);}
 static Object menu(String name)throws Exception{var f=ClientMenu.class.getDeclaredField(name);f.setAccessible(true);return f.get(null);}
 static int results()throws Exception{var m=ClientMenu.class.getDeclaredMethod("searchResults");m.setAccessible(true);return ((List<?>)m.invoke(null)).size();}
 static void key(int code,int text,int action,int mods){var g=Minecraft.getInstance();g.keyboardHandler.keyPress(g.getWindow().handle(),action,new KeyEvent(code,text,mods));}
 static void press(int code){key(code,0,InputConstants.PRESS,0);key(code,0,InputConstants.RELEASE,0);}
 static void text(String s){for(char c:s.toCharArray()){int scan=Character.isLetter(c)?InputConstants.KEY_A+Character.toLowerCase(c)-'a':c==' '?InputConstants.KEY_SPACE:0;key(scan,c,InputConstants.PRESS,0);key(scan,c,InputConstants.RELEASE,0);}}
 static void replace(String s){key(InputConstants.KEY_A,'a',InputConstants.PRESS,0xC0);text(s);}
 static void click(int x,int y){var g=Minecraft.getInstance();float scale=Math.min(1F,Math.min((g.getWindow().getGuiScaledWidth()-20F)/460,(g.getWindow().getGuiScaledHeight()-20F)/410));ClientMenu.click((g.getWindow().getGuiScaledWidth()-460*scale)/2+x*scale,(g.getWindow().getGuiScaledHeight()-410*scale)/2+y*scale);}
 static void stopKeys(){var g=Minecraft.getInstance();for(var k:new net.minecraft.client.KeyMapping[]{g.options.keyUp,g.options.keyDown,g.options.keyLeft,g.options.keyRight,g.options.keyJump,g.options.keyShift,g.options.keySprint})k.setDown(false);}
 public static void packet(Packet<?> packet){
  if(!audit||!FreeCam.active())return;
  if(packet instanceof ServerboundMovePlayerPacket p){positionPackets++;
   check(Math.abs(p.getX(body.x)-body.x)<.04&&Math.abs(p.getZ(body.z)-body.z)<.04,"camera position leaked to movement packets");
   check(Math.abs(p.getYRot(bodyYaw)-bodyYaw)<.01&&Math.abs(p.getXRot(bodyPitch)-bodyPitch)<.01,"camera rotation leaked to server");
  }
  if(packet instanceof ServerboundPlayerInputPacket p){inputPackets++;check(p.input().equals(Input.EMPTY),"camera controls leaked to server input");}
 }
 public static void tick(){var g=Minecraft.getInstance();
  if(g.player==null||g.level==null){if(!connecting&&g.gui.overlay()==null){connecting=true;net.minecraft.client.gui.screens.ConnectScreen.startConnecting(g.gui.screen(),g,new net.minecraft.client.multiplayer.resolver.ServerAddress("127.0.0.1",25568),new net.minecraft.client.multiplayer.ServerData("Faster 1.4.0 check","127.0.0.1:25568",net.minecraft.client.multiplayer.ServerData.Type.OTHER),false,null);}return;}
  try{
   if(!ready){ready=true;g.options.pauseOnLostFocus=false;g.options.guiScale().set(2);g.options.setCameraType(CameraType.FIRST_PERSON);PlayerDisplaySettings.setAutoBridgeEnabled(false);PlayerDisplaySettings.setAutoHitEnabled(false);XraySettings.setEnabled(false);PlayerDisplaySettings.setEnabled(false);FreeCamSettings.setKey(InputConstants.KEY_F6);g.player.connection.sendCommand("tp BridgeLive 0.5 81 0.5 0 0");}
   g.gui.toastManager().clear();ticks++;
   if(ticks==40){press(InputConstants.KEY_F6);check(FreeCam.active(),"default F6 did not activate");press(InputConstants.KEY_F6);check(!FreeCam.active(),"default F6 did not deactivate");pass("default F6 toggles both directions");}
   if(ticks==50){body=g.player.position();press(InputConstants.KEY_P);check(ClientMenu.isOpen()&&(int)menu("category")==0&&(boolean)menu("searchFocused"),"default search not focused");check(menu("query").equals(""),"query not reset");check(results()==7,"module missing from search");}
   if(ticks==60){shot("search-default.png");pass("menu opens to focused search with seven modules");}
   if(ticks==65){text("esp");check(ClientMenu.isOpen()&&results()==1&&menu("query").equals("esp"),"P closed menu during search");}
   if(ticks==67){text(" w");check(results()==0,"no-results query ignored");check(g.player.input.keyPresses.equals(Input.EMPTY),"typing moved player");}
   if(ticks==70){shot("search-empty.png");check(g.player.position().distanceTo(body)<.04,"search typing changed body position");}
   if(ticks==75){replace("FREECAM");check(results()==1,"compact/case-insensitive freecam query");}
   if(ticks==80){shot("search-freecam.png");pass("typed ESP/P, no results, Ctrl+A replacement, compact FreeCam matching and movement capture");}
   if(ticks==85){click(330,129);press(InputConstants.KEY_P);check(FreeCamSettings.key()==InputConstants.KEY_F6,"reserved key accepted");press(InputConstants.KEY_K);check(FreeCamSettings.key()==InputConstants.KEY_K,"letter key binding failed");}
   if(ticks==90){shot("freecam-keybind.png");click(180,109);check(FreeCam.active(),"menu toggle did not activate");}
   if(ticks==100)press(InputConstants.KEY_ESCAPE);
   if(ticks==110){check(!ClientMenu.isOpen(),"menu did not close");body=g.player.position();bodyYaw=g.player.getYRot();bodyPitch=g.player.getXRot();start=FreeCam.position(1);audit=true;g.options.keyUp.setDown(true);}
   if(ticks==130){stopKeys();check(FreeCam.position(1).z-start.z>10,"camera did not fly through wall");check(g.player.position().distanceTo(body)<.04,"body moved during freecam");check(g.player.input.keyPresses.equals(Input.EMPTY),"player input not empty");check(g.gameRenderer.mainCamera().isDetached(),"camera not detached");
    var mouse=(MouseAccess)g.mouseHandler;mouse.check$dx(200);mouse.check$dy(100);mouse.check$turn(.016);check(Math.abs(FreeCam.yaw()-bodyYaw)>1,"mouse did not turn camera");check(g.player.getYRot()==bodyYaw&&g.player.getXRot()==bodyPitch,"mouse turned real player");
    pass("camera crosses solid wall while body/input/rotation remain unchanged; real mouse path turns camera");
    start=FreeCam.position(1);g.options.keyUp.setDown(true);g.options.keyRight.setDown(true);g.options.keyJump.setDown(true);g.options.keySprint.setDown(true);
   }
   if(ticks==143){stopKeys();check(FreeCam.position(1).distanceTo(start)>15,"fast diagonal flight failed");}
   if(ticks==145){focused=false;}
   if(ticks==147){paused=FreeCam.position(1);g.options.keyUp.setDown(true);}
   if(ticks==153){check(FreeCam.position(1).distanceTo(paused)<.001,"unfocused camera moved");stopKeys();focused=true;}
   if(ticks==155){audit=false;press(InputConstants.KEY_K);check(!FreeCam.active(),"rebound toggle did not stop freecam");}
   if(ticks==160){check(g.gameRenderer.mainCamera().position().distanceTo(g.player.getEyePosition())<.1,"return camera not at real player");check(!g.gameRenderer.mainCamera().isDetached(),"detached flag not restored");pass("focus loss stops flight and rebound key restores normal camera");}
   if(ticks==165){press(InputConstants.KEY_K);key(InputConstants.KEY_K,'k',InputConstants.REPEAT,0);check(FreeCam.active(),"key repeat toggled mode");}
   if(ticks==166)press(InputConstants.KEY_K);
   if(ticks==170){body=g.player.position();g.options.keyUp.setDown(true);}
   if(ticks==180){stopKeys();check(g.player.position().distanceTo(body)>.5,"ordinary movement did not resume");pass("key repeat does not toggle; normal movement resumes");}
   if(ticks==190)g.player.connection.sendCommand("tp BridgeLive 0.5 81 0.5 0 0");
   if(ticks==210)press(InputConstants.KEY_K);
   if(ticks==215){g.setScreenAndShow(new InventoryScreen(g.player));paused=FreeCam.position(1);g.options.keyUp.setDown(true);}
   if(ticks==230){check(FreeCam.position(1).distanceTo(paused)<.001,"camera moved while inventory open");stopKeys();g.setScreenAndShow(null);}
   if(ticks==235){press(InputConstants.KEY_K);pass("inventory pauses freecam controls");}
   if(ticks==240){press(InputConstants.KEY_K);g.options.keyJump.setDown(true);XraySettings.setEnabled(true);for(var ore:OreType.values())XraySettings.setOreEnabled(ore,ore==OreType.DIAMOND);}
   if(ticks==270){stopKeys();g.options.keyDown.setDown(true);}
   if(ticks==280){var f=Xray.class.getDeclaredField("meshCamera");f.setAccessible(true);check(((Vec3)f.get(null)).distanceTo(g.player.getEyePosition())>10,"Xray candidate cache did not follow detached camera translation");pass("Xray selection follows a translating detached camera");}
   if(ticks==285){stopKeys();FreeCam.turn(0,65/.15);}
   if(ticks==290){XraySettings.setEnabled(false);for(var ore:OreType.values())XraySettings.setOreEnabled(ore,true);}
   if(ticks==300)shot("freecam-detached.png");
   if(ticks==310)press(InputConstants.KEY_K);
   if(ticks==315)shot("camera-restored.png");
   if(ticks==320)press(InputConstants.KEY_K);
   if(ticks==325)g.player.setHealth(0);
   if(ticks==326){check(!FreeCam.active(),"death did not return camera");g.player.setHealth(20);g.setScreenAndShow(null);pass("death deactivates freecam");}
   if(ticks==335){g.options.setCameraType(CameraType.THIRD_PERSON_BACK);FreeCam.enable();}
   if(ticks==338)FreeCam.disable();
   if(ticks==342){check(g.options.getCameraType()==CameraType.THIRD_PERSON_BACK&&g.gameRenderer.mainCamera().isDetached(),"previous perspective lost");g.options.setCameraType(CameraType.FIRST_PERSON);pass("original third-person setting is preserved");}
   if(ticks==345){press(InputConstants.KEY_P);check((int)menu("category")==0&&menu("query").equals(""),"reopening did not reset search");ClientMenu.scroll(-100);check((int)menu("searchOffset")==1,"search scrolling");ClientMenu.scroll(-1);check((int)menu("searchOffset")==2,"last modules inaccessible");shot("search-scrolled.png");}
   if(ticks==350){press(InputConstants.KEY_ESCAPE);FreeCamSettings.setKey(InputConstants.KEY_F6);check(positionPackets>0,"no real movement packets observed");pass("movement packets stay at body; search scrolling exposes all seven modules");Files.writeString(Path.of("/tmp/faster140-live/results.txt"),String.join("\n",passes)+"\nposition packets="+positionPackets+", input packets="+inputPackets+"\n");System.out.println("FASTER 1.4.0 ALL CHECKS COMPLETE");g.stop();}
  }catch(Exception e){throw new RuntimeException(e);}
 }
 static void shot(String name)throws Exception{Files.createDirectories(Path.of("/tmp/faster140-preview/screenshots"));var g=Minecraft.getInstance();Screenshot.grab(new java.io.File("/tmp/faster140-preview"),name,g.gameRenderer.mainRenderTarget(),1,c->System.out.println("FASTER SCREENSHOT "+c.getString()));}
}
