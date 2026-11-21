package check;
import java.nio.file.*;
import java.util.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.fastersprint.*;
import net.minecraft.client.*;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
public final class JumpCheck {
 static final String[] CASES={"normal","quick","boost-normal","boost-quick","ceiling","step","hold","quickfall","wall","freecam","water"};
 static final List<String> results=new ArrayList<>(),trace=new ArrayList<>();
 static boolean connecting,ready,started;static int warm=80,stage=-1,t,corrections,hops,peakTick;static double start,peak,first,minVelocity,last,normalPeak,boostPeak;
 static Minecraft g(){return Minecraft.getInstance();}
 static void check(boolean v,String s){if(!v)throw new AssertionError(s);}
 static void stop(){var o=g().options;for(var k:new KeyMapping[]{o.keyUp,o.keyDown,o.keyLeft,o.keyRight,o.keyJump,o.keyShift,o.keySprint})k.setDown(false);}
 static void command(String s){g().player.connection.sendCommand(s);}
 static void key(int scan,int code){var g=g();g.keyboardHandler.keyPress(g.getWindow().handle(),1,new KeyEvent(scan,code,0));g.keyboardHandler.keyPress(g.getWindow().handle(),0,new KeyEvent(scan,code,0));}
 static void click(int x,int y){var g=g();float s=Math.min(1F,Math.min((g.getWindow().getGuiScaledWidth()-20F)/460,(g.getWindow().getGuiScaledHeight()-20F)/410));ClientMenu.click((g.getWindow().getGuiScaledWidth()-460*s)/2+x*s,(g.getWindow().getGuiScaledHeight()-410*s)/2+y*s);}
 static Object field(String n)throws Exception{var f=ClientMenu.class.getDeclaredField(n);f.setAccessible(true);return f.get(null);}
 static void shot(String name)throws Exception{Files.createDirectories(Path.of("/tmp/faster170-live/screenshots"));Screenshot.grab(new java.io.File("/tmp/faster170-live"),name,g().gameRenderer.mainRenderTarget(),1,c->{});}
 static void pass(String s){results.add("PASS "+s);System.out.println("JUMP PASS "+s);}
 public static void correction(ClientboundPlayerPositionPacket p){if(stage>=0&&stage<CASES.length&&t>20)corrections++;}
 static void begin()throws Exception{stop();FreeCam.disable();stage++;t=0;corrections=0;hops=0;peak=0;first=0;minVelocity=0;peakTick=0;
  if(stage==CASES.length){finish();return;}
  String n=CASES[stage];QuickJumpSettings.setEnabled(!n.equals("normal")&&!n.equals("boost-normal"));QuickFallSettings.setEnabled(n.equals("quickfall"));WallClimbSettings.setEnabled(n.equals("wall"));
  command("effect clear BridgeLive jump_boost");if(n.startsWith("boost"))command("effect give BridgeLive jump_boost infinite 0 true");
  command("tp BridgeLive "+(n.equals("ceiling")?"3.5 81 -6.5":n.equals("step")?"11.5 81 1.69":n.equals("wall")?"8.5 81 2.69":n.equals("water")?"33.5 81 -6.5":"0.5 81 -8.5")+" 0 0");
  System.out.println("JUMP START "+n);
 }
 public static void tick(){var g=g();
  if(g.player==null||g.level==null){if(!connecting&&g.gui.overlay()==null){connecting=true;net.minecraft.client.gui.screens.ConnectScreen.startConnecting(g.gui.screen(),g,new net.minecraft.client.multiplayer.resolver.ServerAddress("127.0.0.1",25568),new net.minecraft.client.multiplayer.ServerData("QuickJump check","127.0.0.1:25568",net.minecraft.client.multiplayer.ServerData.Type.OTHER),false,null);}return;}
  try{
   g.options.pauseOnLostFocus=false;g.gui.toastManager().clear();
   if(!ready){ready=true;g.options.guiScale().set(2);g.options.autoJump().set(false);PlayerDisplaySettings.setGuiMoveEnabled(false);PlayerDisplaySettings.setAutoBridgeEnabled(false);PlayerDisplaySettings.setAutoHitEnabled(false);PlayerDisplaySettings.setEnabled(false);XraySettings.setEnabled(false);BedEspSettings.setEnabled(false);QuickFallSettings.setEnabled(false);QuickJumpSettings.setEnabled(false);WallClimbSettings.setEnabled(false);FreeCam.disable();command("gamemode survival BridgeLive");command("tp BridgeLive 0.5 81 -20.5 0 0");}
   if(warm-->0||g.gui.overlay()!=null)return;t++;
   if(stage==-1){
    if(t==1){command("fill -4 80 -12 40 80 12 stone");command("fill -4 81 -12 40 90 2 air");command("fill 2 83 -8 4 83 -6 stone");command("fill 10 81 2 12 81 8 stone");command("fill 10 82 2 12 90 8 air");command("fill 32 81 -8 34 84 -6 water");key(InputConstants.KEY_P,'p');for(char c:"quickjump".toCharArray())key(InputConstants.KEY_A+c-'a',c);check(field("query").equals("quickjump"),"search text");var m=ClientMenu.class.getDeclaredMethod("searchResults");m.setAccessible(true);check(((List<?>)m.invoke(null)).size()==1,"search missing/duplicated");click(180,109);check(QuickJumpSettings.enabled(),"search toggle on");click(180,109);check(!QuickJumpSettings.enabled(),"search toggle off");}
    if(t==12)shot("quickjump-search.png");
    if(t==14){click(40,90);ClientMenu.scroll(-1);click(180,345);check(QuickJumpSettings.enabled(),"Movement toggle on");click(180,345);check(!QuickJumpSettings.enabled(),"Movement toggle off");pass("QuickJump searchable and toggleable in scrolled Movement");}
    if(t==26)shot("movement-quickjump.png");
    if(t==28){key(InputConstants.KEY_ESCAPE,0);begin();}return;
   }
   if(stage>=CASES.length)return;String n=CASES[stage];
   if(t==20){start=g.player.getY();last=start;check(g.player.isAlive(),"actor dead");if(n.equals("water"))check(g.player.isInWater(),"water scene missing");else check(g.player.onGround(),"actor not grounded at takeoff "+n);
    if(n.equals("freecam"))FreeCam.enable();if(n.equals("step")||n.equals("wall"))g.options.keyUp.setDown(true);g.options.keyJump.setDown(true);
   }
   if(n.equals("step")&&t==38)g.options.keyUp.setDown(false);
   if(t==21){first=g.player.getY()-start;if(!Set.of("hold","wall","water","freecam").contains(n))g.options.keyJump.setDown(false);
    if(Set.of("quick","boost-quick","step","quickfall").contains(n))check(Math.abs(g.player.getPosition(0).y-g.player.getY())<.00001,"ascent still visually interpolated "+n);
   }
   if(t>=21){double y=g.player.getY()-start;if(y>peak){peak=y;peakTick=t-20;}if(g.player.getY()-last>1)hops++;last=g.player.getY();minVelocity=Math.min(minVelocity,g.player.getDeltaMovement().y);
    trace.add(n+","+(t-20)+","+y+","+g.player.getDeltaMovement().y+","+g.player.onGround());
    if(t==(n.equals("hold")?90:55)){
     check(corrections==0,"server corrections "+n+": "+corrections);
     switch(n){
      case "normal"->{normalPeak=peak;check(first<.5&&peak>1.2&&peak<1.3&&peakTick>1,"reference jump invalid");}
      case "quick","quickfall"->{check(Math.abs(peak-normalPeak)<.000001&&Math.abs(first-normalPeak)<.000001&&peakTick==1,"incorrect instant peak "+first+" "+peak);if(n.equals("quickfall"))check(minVelocity> -1,"QuickFall overrode jump");}
      case "boost-normal"->{boostPeak=peak;check(peak>normalPeak,"boost reference invalid");}
      case "boost-quick"->check(Math.abs(peak-boostPeak)<.000001&&peakTick==1,"boost peak changed "+peak+" expected "+boostPeak);
      case "ceiling"->check(peak<=.200001&&first>.19,"ceiling collision failed "+peak);
      case "step"->check(g.player.getY()>81.99&&g.player.getY()<82.01&&g.player.getZ()>3,"failed to get onto one-block ledge "+g.player.position());
      case "hold"->check(hops>=3&&peak<=normalPeak+.000001,"repeated jumps boosted height or stopped "+hops+" "+peak);
      case "wall"->check(first<.6&&peak>2,"Wall Climb jump interaction "+first+" "+peak);
      case "freecam"->check(Math.abs(g.player.getY()-start)<.00001&&FreeCam.position(1).y-g.player.getEyeY()>5,"free camera/body interaction");
      case "water"->check(first<.2,"water jump was accelerated "+first);
     }
     pass(n+": firstTickRise="+first+", peak="+peak+", peakTick="+peakTick+", minVelocity="+minVelocity+", hops="+hops+", corrections="+corrections);begin();
    }
   }
  }catch(Exception e){throw new RuntimeException(e);}
 }
 static void finish()throws Exception{stop();FreeCam.disable();QuickJumpSettings.setEnabled(false);QuickFallSettings.setEnabled(false);WallClimbSettings.setEnabled(false);command("effect clear BridgeLive jump_boost");Files.writeString(Path.of("/tmp/faster170-live/results.txt"),String.join("\n",results)+"\n");Files.writeString(Path.of("/tmp/faster170-live/trajectories.csv"),"case,tick,rise,velocityY,onGround\n"+String.join("\n",trace)+"\n");System.out.println("JUMP ALL CHECKS COMPLETE");g().stop();}
}
