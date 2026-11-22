package check;
import java.nio.file.*;
import java.util.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.fastersprint.*;
import net.minecraft.client.*;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
public final class AimCheck {
 public static boolean hold,focus=true;public static int lastReadKey;
 static boolean connecting,ready;static int warm=100,t,rotations,interactions,aimFrames;static RemotePlayer near,far;
 static final List<String> results=new ArrayList<>();static final Map<Integer,Integer> moves=new HashMap<>();
 static Minecraft g(){return Minecraft.getInstance();}
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
 static void pass(String s){results.add("PASS "+s);System.out.println("AIM PASS "+s);}
 static void command(String s){g().player.connection.sendCommand(s);}
 static Object menu(String s)throws Exception{var f=ClientMenu.class.getDeclaredField(s);f.setAccessible(true);return f.get(null);}
 static Object target()throws Exception{var f=AimLock.class.getDeclaredField("target");f.setAccessible(true);return f.get(null);}
 static void key(int scan,int code){var g=g();g.keyboardHandler.keyPress(g.getWindow().handle(),1,new KeyEvent(scan,code,0));g.keyboardHandler.keyPress(g.getWindow().handle(),0,new KeyEvent(scan,code,0));}
 static void click(int x,int y){var w=g().getWindow();float s=Math.min(1F,Math.min((w.getGuiScaledWidth()-20F)/460,(w.getGuiScaledHeight()-20F)/410));ClientMenu.click((w.getGuiScaledWidth()-460*s)/2+x*s,(w.getGuiScaledHeight()-410*s)/2+y*s);}
 static void shot(String s)throws Exception{Files.createDirectories(Path.of("/tmp/faster190-live/screenshots"));Screenshot.grab(new java.io.File("/tmp/faster190-live"),s,g().gameRenderer.mainRenderTarget(),1,c->{});}
 static RemotePlayer spawn(String name,int id,double dx,double dz){var p=new RemotePlayer(g().level,new com.mojang.authlib.GameProfile(UUID.nameUUIDFromBytes(name.getBytes()),name));p.setId(id);p.setPos(-255.5+dx,120,-255.5+dz);p.setOldPosAndRot();g().level.addEntity(p);return p;}
 static void move(RemotePlayer p,double dx,double dz){p.setPos(-255.5+dx,120,-255.5+dz);p.setOldPosAndRot();}
 static void look(float yaw,float pitch){g().player.setYRot(yaw);g().player.setXRot(pitch);}
 static double wrap(double v){v%=360;if(v>=180)v-=360;if(v< -180)v+=360;return v;}
 static void aimed(RemotePlayer p)throws Exception{check(target()==p,"wrong nearest target");check(aimFrames>0,"no actual camera-frame aiming observed");}
 public static void frame(Camera camera,float partial){
  try{var g=g();if(camera!=g.gameRenderer.mainCamera()||!hold||!focus||!AimLockSettings.enabled()||g.player==null||g.gui.screen()!=null||g.gui.overlay()!=null||ClientMenu.isOpen()||FreeCam.active())return;
   var p=(net.minecraft.world.entity.player.Player)target();if(p==null)return;
   var delta=p.getEyePosition(partial).subtract(g.player.getEyePosition(partial)).normalize();double body=g.player.getViewVector(1).normalize().dot(delta);
   var d=p.getEyePosition(partial).subtract(camera.position()).normalize();var f=camera.forwardVector();double visible=d.dot(new Vec3(f.x(),f.y(),f.z()));
   check(body>.99999&&visible>.99999,"render-frame head mismatch body="+body+" camera="+visible+" yaw="+g.player.getYRot()+" head="+p.getEyePosition(partial)+" eye="+g.player.getEyePosition(partial));aimFrames++;
  }catch(Exception e){throw new RuntimeException(e);}
 }
 public static void packet(Packet<?> packet){var g=g();if(g.player==null||t<80)return;
  if(packet instanceof ServerboundMovePlayerPacket p){moves.merge(g.player.tickCount,1,Integer::sum);check(moves.get(g.player.tickCount)<=1,"extra movement packet in tick");if(p.hasRotation()){rotations++;check(Math.abs(wrap(p.getYRot(g.player.getYRot())-g.player.getYRot()))<.001,"rotation differs from visible player yaw");check(Math.abs(p.getXRot(g.player.getXRot())-g.player.getXRot())<.001,"rotation differs from visible player pitch");}}
  if(packet instanceof ServerboundInteractPacket)interactions++;
 }
 public static void tick(){var g=g();
  if(g.player==null||g.level==null){if(!connecting&&g.gui.overlay()==null){connecting=true;net.minecraft.client.gui.screens.ConnectScreen.startConnecting(g.gui.screen(),g,new net.minecraft.client.multiplayer.resolver.ServerAddress("127.0.0.1",25568),new net.minecraft.client.multiplayer.ServerData("Aim Lock verification","127.0.0.1:25568",net.minecraft.client.multiplayer.ServerData.Type.OTHER),false,null);}return;}
  try{
   g.options.pauseOnLostFocus=false;g.gui.toastManager().clear();
   if(!ready){ready=true;g.options.guiScale().set(2);g.options.renderDistance().set(3);g.options.setCameraType(CameraType.FIRST_PERSON);PlayerDisplaySettings.setAutoBridgeEnabled(false);PlayerDisplaySettings.setAutoHitEnabled(false);PlayerDisplaySettings.setEnabled(false);PlayerDisplaySettings.setGuiMoveEnabled(false);XraySettings.setEnabled(false);BedEspSettings.setEnabled(false);TintSettings.SKY.setEnabled(false);QuickJumpSettings.setEnabled(false);QuickFallSettings.setEnabled(false);WallClimbSettings.setEnabled(false);FreeCam.disable();AimLockSettings.setEnabled(false);AimLockSettings.setKey(InputConstants.KEY_R);command("gamemode survival BridgeLive");command("tp BridgeLive -64.5 81 -64.5 0 0");command("forceload add -272 -272 -240 -240");command("time set 6000");command("weather clear");}
   if(warm-->0||g.gui.overlay()!=null)return;t++;
   if(t==1){command("fill -272 119 -272 -240 119 -240 stone");command("fill -272 120 -272 -240 125 -240 air");}
   if(t==10)command("tp BridgeLive -255.5 120 -255.5 90 25");
   if(t==30){near=spawn("NearHead",2100001,2,8);far=spawn("FarHead",2100002,-6,10);check(g.level.players().contains(near)&&g.level.players().contains(far),"test players not registered");}
   if(t==40){key(InputConstants.KEY_P,'p');for(char c:"aimlock".toCharArray())key(InputConstants.KEY_A+c-'a',c);var m=ClientMenu.class.getDeclaredMethod("searchResults");m.setAccessible(true);check(((List<?>)m.invoke(null)).size()==1,"search module missing/duplicated");click(180,109);check(AimLockSettings.enabled(),"search toggle on");click(180,109);check(!AimLockSettings.enabled(),"search toggle off");}
   if(t==55)shot("aim-lock-search.png");
   if(t==60){click(40,150);check((int)menu("category")==3,"Combat tab");click(350,139);key(InputConstants.KEY_F8,0);check(AimLockSettings.key()==InputConstants.KEY_F8,"function key bind");click(350,139);key(InputConstants.KEY_A,'a');check(AimLockSettings.key()==InputConstants.KEY_A,"letter A binding");click(350,139);key(InputConstants.KEY_R,'r');check(AimLockSettings.key()==InputConstants.KEY_R&&!ClientMenu.capturesMovement(),"R binding/capture release");click(180,117);check(AimLockSettings.enabled(),"combat switch");
    click(40,90);click(350,261);key(InputConstants.KEY_B,'b');check(PlayerDisplaySettings.autoBridgeKey()==InputConstants.KEY_B,"bridge bind regression");click(40,120);click(350,291);key(InputConstants.KEY_F6,0);check(FreeCamSettings.key()==InputConstants.KEY_F6,"camera bind regression");click(40,150);pass("Search/Combat switches, letter/function hold bindings, and existing Bridge/Free Cam bindings work");}
   if(t==75)shot("combat-aim-lock.png");
   if(t==80){key(InputConstants.KEY_ESCAPE,0);look(90,25);}
   if(t==100){check(target()==null&&Math.abs(g.player.getYRot()-90)<.01,"aim changed without hold");pass("enabled module leaves manual view alone while hold key is released");hold=true;}
   if(t==120){aimed(near);check(lastReadKey==InputConstants.KEY_R,"wrong hold key read");pass("nearest visible player's head aligns both real player view and camera");shot("nearest-head.png");move(far,-1,3);}
   if(t==140){aimed(far);pass("moving a player closer switches to the new closest head");far.setPose(Pose.CROUCHING);far.refreshDimensions();}
   if(t==160){aimed(far);check(g.player.getXRot()>1,"crouched head height ignored");pass("tracks crouching head height");far.setInvisible(true);}
   if(t==180){aimed(near);pass("invisible nearest player is skipped");far.setInvisible(false);far.setHealth(0);}
   if(t==200){aimed(near);pass("dead nearest player is skipped");far.discard();far=spawn("FarVisible",2100003,-6,10);command("fill -255 120 -252 -254 123 -252 stone");}
   if(t==230){aimed(far);check(!g.player.hasLineOfSight(near),"occlusion wall missed close head");pass("blocked closer head is skipped for unobstructed farther player");shot("wall-occlusion.png");move(far,0,-4);}
   if(t==260){aimed(far);check(Math.abs(wrap(g.player.getYRot()-180))<.01,"behind-player selection failed");pass("nearest unobstructed head can be selected behind the original view");hold=false;look(77,12);}
   if(t==280){check(target()==null&&Math.abs(g.player.getYRot()-77)<.01,"release did not restore manual control");pass("releasing hold immediately stops tracking");hold=true;key(InputConstants.KEY_P,'p');look(11,22);}
   if(t==300){check(target()==null&&Math.abs(g.player.getYRot()-11)<.01,"aim active in menu");pass("P menu pauses aim");key(InputConstants.KEY_ESCAPE,0);FreeCam.enable();look(33,10);}
   if(t==320){check(target()==null&&Math.abs(g.player.getYRot()-33)<.01,"aim moved body during Free Cam");pass("Free Cam pauses aim without rotating the body");FreeCam.disable();focus=false;look(55,0);}
   if(t==340){check(target()==null&&Math.abs(g.player.getYRot()-55)<.01,"aim active unfocused");pass("focus loss pauses aim");focus=true;AimLockSettings.setEnabled(false);look(66,15);}
   if(t==360){check(target()==null&&Math.abs(g.player.getYRot()-66)<.01,"disabled aim changed view");pass("disabled module leaves view alone even with key held");AimLockSettings.setEnabled(true);}
   if(t==380){aimed(far);near.setHealth(0);far.setHealth(0);look(88,0);}
   if(t==400){check(target()==null&&Math.abs(g.player.getYRot()-88)<.01,"stale dead target");pass("no valid players clears the target without changing view");check(rotations>0&&interactions==0,"look packet/attack audit");pass("vanilla look packets match visible angles; at most one movement packet per tick; zero attack interactions; lookPackets="+rotations+", alignedCameraFrames="+aimFrames);hold=false;AimLockSettings.setEnabled(false);AimLockSettings.setKey(InputConstants.KEY_R);Files.writeString(Path.of("/tmp/faster190-live/results.txt"),String.join("\n",results)+"\n");System.out.println("AIM ALL CHECKS COMPLETE");g.stop();}
  }catch(Exception e){throw new RuntimeException(e);}
 }
}
