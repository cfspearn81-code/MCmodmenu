package check;
import java.nio.file.*;
import java.util.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.fastersprint.*;
import net.minecraft.client.*;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
public final class MenuCheck {
 static boolean connecting,ready,framePending;static int warm=80,t,rawTicks,armed,sequence,testedFrames,menuFrames,pauseFrames;public static int blurs;
 static final List<String> results=new ArrayList<>();
 static Minecraft g(){return Minecraft.getInstance();}
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 static Object field(String n)throws Exception{var f=ClientMenu.class.getDeclaredField(n);f.setAccessible(true);return f.get(null);}
 static void key(int scan,int code){var g=g();g.keyboardHandler.keyPress(g.getWindow().handle(),1,new KeyEvent(scan,code,0));g.keyboardHandler.keyPress(g.getWindow().handle(),0,new KeyEvent(scan,code,0));}
 static void number(int y,String value){click(375,y);for(char c:value.toCharArray())key(InputConstants.KEY_1,c);key(InputConstants.KEY_RETURN,13);}
 static void click(int x,int y){var g=g();float s=Math.min(1F,Math.min((g.getWindow().getGuiScaledWidth()-20F)/460,(g.getWindow().getGuiScaledHeight()-20F)/410));ClientMenu.click((g.getWindow().getGuiScaledWidth()-460*s)/2+x*s,(g.getWindow().getGuiScaledHeight()-410*s)/2+y*s);}
 static void visible(String... expected)throws Exception{Class<?> tool=Class.forName("net.fastersprint.ClientMenu$Tool");var m=ClientMenu.class.getDeclaredMethod("visible",tool);m.setAccessible(true);Set<String> actual=new HashSet<>();for(var v:tool.getEnumConstants())if((boolean)m.invoke(null,v))actual.add(v.toString());check(actual.equals(Set.of(expected)),"visible modules "+actual);}
 static void pass(String s){results.add("PASS "+s);System.out.println("MENU PASS "+s);}
 static void shot(String name)throws Exception{Files.createDirectories(Path.of("/tmp/faster161-live/screenshots"));Screenshot.grab(new java.io.File("/tmp/faster161-live"),name,g().gameRenderer.mainRenderTarget(),1,c->{});}
 public static void tick(){var g=g();
  if(g.player==null||g.level==null){if(!connecting&&g.gui.overlay()==null){connecting=true;ConnectScreen.startConnecting(g.gui.screen(),g,new net.minecraft.client.multiplayer.resolver.ServerAddress("127.0.0.1",25568),new net.minecraft.client.multiplayer.ServerData("Menu check","127.0.0.1:25568",net.minecraft.client.multiplayer.ServerData.Type.OTHER),false,null);}return;}
  try{
   g.options.pauseOnLostFocus=false;g.gui.toastManager().clear();
   if(!ready){ready=true;g.options.guiScale().set(2);g.options.menuBackgroundBlurriness().set(5);PlayerDisplaySettings.setAutoBridgeEnabled(false);PlayerDisplaySettings.setAutoHitEnabled(false);PlayerDisplaySettings.setGuiMoveEnabled(false);PlayerDisplaySettings.setEnabled(false);XraySettings.setEnabled(false);BedEspSettings.setEnabled(false);QuickFallSettings.setEnabled(false);WallClimbSettings.setEnabled(false);FreeCam.disable();g.player.connection.sendCommand("tp BridgeLive 0.5 81 -20.5 0 10");}
   if(warm-->0||g.gui.overlay()!=null)return;if(++rawTicks%4!=0)return;t++;
   if(t==1){check(g.gui.screen()==null,"join screen remained");key(InputConstants.KEY_P,'p');check(ClientMenu.isOpen()&&(int)field("category")==0,"search default");click(40,120);visible("XRAY","PLAYERS","BEDESP","FREECAM");check((int)field("visualPage")==0,"visuals default");pass("Visuals contains exactly Xray, Player ESP, BedEsp and Free Cam");}
   if(t==5)shot("visuals-overview.png");
   if(t==7){click(180,101);check(XraySettings.enabled(),"xray on");click(180,101);click(180,157);check(PlayerDisplaySettings.enabled(),"esp on");click(180,157);click(180,213);check(BedEspSettings.enabled(),"bed on");click(180,213);click(180,269);check(FreeCam.active(),"camera on");click(180,269);check(!FreeCam.active(),"camera off");click(350,289);key(InputConstants.KEY_F7,0);check(FreeCamSettings.key()==InputConstants.KEY_F7,"camera key");click(350,289);key(InputConstants.KEY_F6,0);pass("all four Visuals switches and Free Cam key binding work");}
   if(t==10){click(240,65);visible("PLAYERS");number(165,"650");check(PlayerDisplaySettings.range()==650,"esp range");number(227,"12");check(PlayerDisplaySettings.nameLimit()==12,"esp names");}
   if(t==14)shot("visuals-player-settings.png");
   if(t==17){click(320,65);visible("XRAY");number(165,"321");check(XraySettings.range()==321,"xray range");boolean before=XraySettings.oreEnabled(OreType.COAL);click(150,250);check(before!=XraySettings.oreEnabled(OreType.COAL),"ore filter");click(150,250);}
   if(t==21)shot("visuals-xray-settings.png");
   if(t==24){click(410,65);visible("BEDESP");number(165,"1");check(BedEspSettings.range()==1,"bed min");number(165,"5000");check(BedEspSettings.range()==5000,"bed max");number(165,"5001");check(BedEspSettings.range()==5000&&!((String)field("error")).isEmpty(),"invalid range");number(165,"200");pass("Visuals settings preserve ESP range/name limit, Xray radius/filters and BedEsp typed bounds");}
   if(t==28)shot("visuals-bed-settings.png");
   if(t==31){click(40,90);visible("SPEED","GUIMOVE","BRIDGE","WALLCLIMB","QUICKFALL");pass("Movement contains five movement modules without scrolling");}
   if(t==35)shot("movement.png");
   if(t==38)ClientMenu.toggle();
   if(t==40){armed=1;sequence=0;}
   if(t>80&&testedFrames>=8)finish();
  }catch(Exception e){throw new RuntimeException(e);}
 }
 public static void beforeFrame(){blurs=0;framePending=false;if(armed==0)return;
  try{
   var g=g();g.gui.setScreen(null);if(ClientMenu.isOpen())ClientMenu.toggle();
   if(sequence<8){
    PlayerDisplaySettings.setGuiMoveEnabled((sequence&1)!=0);ClientMenu.toggle();check(ClientMenu.isOpen(),"menu did not open before frame");
    g.gui.setScreen((sequence&4)==0?new PauseScreen(true):new InventoryScreen(g.player));check(!ClientMenu.isOpen(),"screen did not dismiss immediately");
    if((sequence&2)!=0)ClientMenu.toggle(); // Force stale state to exercise render guard before any tick.
    framePending=true;sequence++;
   }else{armed=0;g.gui.setScreen(null);ClientMenu.toggle();g.gui.setOverlay(new Overlay(){public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor graphics,int x,int y,float delta){}});check(!ClientMenu.isOpen(),"overlay did not dismiss immediately");g.gui.setOverlay(null);pass("overlay opening dismisses the menu before any tick");pass("eight between-tick pause/inventory transitions rendered safely with GuiMove on/off and forced stale menu state");}
  }catch(Exception e){throw new RuntimeException(e);}
 }
 public static void afterFrame(){if(g().player==null)return;check(blurs<=1,"duplicate blur requests "+blurs);
  if(framePending){check(!ClientMenu.isOpen(),"stale menu survived render");check(blurs==(g().gui.screen() instanceof PauseScreen?1:0),"unexpected screen blur count "+blurs);System.out.println("MENU TRANSITION "+sequence+" screen="+g().gui.screen().getClass().getSimpleName()+" blurs="+blurs);check(!g().mouseHandler.isMouseGrabbed(),"menu grabbed mouse from screen");testedFrames++;}
  if(ClientMenu.isOpen()){check(blurs==1,"menu blur missing");menuFrames++;}if(g().gui.screen() instanceof PauseScreen)pauseFrames++;
 }
 static void finish()throws Exception{PlayerDisplaySettings.setGuiMoveEnabled(false);PlayerDisplaySettings.setRange(500);PlayerDisplaySettings.setNameLimit(10);XraySettings.setRange(200);BedEspSettings.setRange(200);pass("at most one blur per frame; menuFrames="+menuFrames+", pauseFrames="+pauseFrames+", transitionFrames="+testedFrames);Files.writeString(Path.of("/tmp/faster161-live/results.txt"),String.join("\n",results)+"\n");System.out.println("MENU ALL CHECKS COMPLETE");g().stop();}
}
