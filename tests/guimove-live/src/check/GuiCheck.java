package check;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.world.inventory.*;
import net.minecraft.network.chat.Component;
import net.fastersprint.PlayerDisplaySettings;
import com.mojang.blaze3d.platform.InputConstants;
public class GuiCheck {
 public static boolean focused=true,held;
 static boolean connecting,started;static int warmup=60,stage=-1,ticks;static double x,z,lastZ;
 static final String[] CASES={"inventory","crafting","chest","chat","pause","options","generic","disabled","unfocused","release"};
 static final List<String> RESULTS=new ArrayList<>();
 public static boolean key(int key){return held&&key==InputConstants.KEY_W;}
 static Screen screen(Minecraft g,String name){return switch(name){
  case "crafting"->new CraftingScreen(new CraftingMenu(1,g.player.getInventory()),g.player.getInventory(),Component.literal("Crafting test"));
  case "chest"->new ContainerScreen(ChestMenu.threeRows(1,g.player.getInventory()),g.player.getInventory(),Component.literal("Chest test"));
  case "chat"->new ChatScreen("",false);
  case "pause","disabled"->new PauseScreen(true);
  case "options"->new OptionsScreen(null,g.options);
  case "generic"->new Screen(Component.literal("Generic screen")){};
  default->new InventoryScreen(g.player);
 };}
 static void begin(Minecraft g)throws Exception {
  stage++;ticks=0;held=false;focused=true;
  if(stage>=CASES.length){g.setScreenAndShow(null);System.out.println("GUI MOVE ALL CASES COMPLETE");return;}
  String name=CASES[stage];PlayerDisplaySettings.setGuiMoveEnabled(!name.equals("disabled"));
  g.setScreenAndShow(screen(g,name));x=g.player.getX();z=g.player.getZ();System.out.println("GUI MOVE START "+name);
 }
 public static void tick(){Minecraft g=Minecraft.getInstance();
  if(g.player==null||g.level==null){if(!connecting&&g.gui.overlay()==null){connecting=true;ConnectScreen.startConnecting(g.gui.screen(),g,new net.minecraft.client.multiplayer.resolver.ServerAddress("127.0.0.1",25568),new net.minecraft.client.multiplayer.ServerData("GuiMove test","127.0.0.1:25568",net.minecraft.client.multiplayer.ServerData.Type.OTHER),false,null);}return;}
  try{
   g.options.pauseOnLostFocus=false;g.gui.toastManager().clear();
   if(!started){started=true;PlayerDisplaySettings.setAutoBridgeEnabled(false);PlayerDisplaySettings.setAutoHitEnabled(false);g.player.connection.sendCommand("tp BridgeLive 0.5 81 0.5 0 0");}
   g.player.setYRot(0);g.player.setXRot(0);
   g.options.keyUp.setDown(false);g.options.keyDown.setDown(false);g.options.keyLeft.setDown(false);g.options.keyRight.setDown(false);g.options.keyJump.setDown(false);g.options.keyShift.setDown(false);g.options.keySprint.setDown(false);
   if(warmup-->0)return;
   if(stage<0){begin(g);return;}
   if(stage>=CASES.length){finishVisual(g);return;}
   String name=CASES[stage];ticks++;
   focused=!name.equals("unfocused");held=ticks>10&&(!name.equals("release")||ticks<35);
   if(ticks==10){x=g.player.getX();z=g.player.getZ();}
   if(g.player.getY()<80.5)throw new AssertionError("test floor missing");
   if(name.equals("chat")&&ticks==20){
    boolean typed=g.gui.screen().charTyped(new net.minecraft.client.input.CharacterEvent('w'));
    var field=g.gui.screen().getFocused();
    if(!typed||!(field instanceof net.minecraft.client.gui.components.EditBox edit)||!edit.getValue().contains("w"))throw new AssertionError("chat typing lost");
    System.out.println("PASS: chat receives W while movement is active");
   }
   if(name.equals("pause")&&ticks==20&&g.gui.isPausing())throw new AssertionError("GuiMove pause override missing");
   if(name.equals("disabled")&&ticks==20&&!g.gui.isPausing())throw new AssertionError("disabled module does not restore pause");
   if(ticks==55)lastZ=g.player.getZ();
   if(ticks>=75){
    held=false;double distance=Math.hypot(g.player.getX()-x,g.player.getZ()-z);
    boolean blocked=name.equals("disabled")||name.equals("unfocused");
    if(blocked?distance>.05:distance<1)throw new AssertionError(name+" movement="+distance);
    if(name.equals("release")&&Math.abs(g.player.getZ()-lastZ)>.01)throw new AssertionError("release stuck");
    String result="{\"screen\":\""+name+"\",\"distance\":"+distance+"}";RESULTS.add(result);
    Files.writeString(Path.of("/tmp/guimove-live/results.json"),"["+String.join(",",RESULTS)+"]");System.out.println("GUI MOVE PASS "+result);
    begin(g);
   }
  }catch(Exception e){throw new RuntimeException(e);}
 }
 static int visualTick;
 static void finishVisual(Minecraft g)throws Exception{
  held=false;focused=true;visualTick++;
  if(visualTick==1){
   PlayerDisplaySettings.setEnabled(true);
   addPlayer(g,"RedTest",net.minecraft.world.scores.TeamColor.RED,-2);
   addPlayer(g,"BlueTest",net.minecraft.world.scores.TeamColor.BLUE,2);
  }
  if(visualTick>65)g.player.setYRot(180);
  if(visualTick==50)net.minecraft.client.Screenshot.grab(new java.io.File("/tmp/guimove-preview"),"tracer-colors.png",g.gameRenderer.mainRenderTarget(),1,c->System.out.println("TRACER SCREENSHOT "+c.getString()));
  if(visualTick==95)net.minecraft.client.Screenshot.grab(new java.io.File("/tmp/guimove-preview"),"tracer-colors-offscreen.png",g.gameRenderer.mainRenderTarget(),1,c->System.out.println("OFFSCREEN TRACER SCREENSHOT "+c.getString()));
  if(visualTick==120)g.stop();
 }
 static void addPlayer(Minecraft g,String name,net.minecraft.world.scores.TeamColor color,int offset){
  var p=new net.minecraft.client.player.RemotePlayer(g.level,new com.mojang.authlib.GameProfile(java.util.UUID.nameUUIDFromBytes(name.getBytes()),name));
  p.setId(950000+offset);p.setPos(g.player.getX()+offset,81,g.player.getZ()+6);
  var team=g.level.getScoreboard().addPlayerTeam(name);team.setColor(Optional.of(color));g.level.getScoreboard().addPlayerToTeam(name,team);g.level.addEntity(p);
 }
}
