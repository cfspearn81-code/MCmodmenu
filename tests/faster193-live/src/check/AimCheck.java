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
 static boolean connecting,ready;static boolean expectTracking;static RemotePlayer distant;static int warm=100,t,rotations,interactions,aimFrames;static RemotePlayer near,far;
 static final List<String> results=new ArrayList<>();static final Map<Integer,Integer> moves=new HashMap<>();
 static Minecraft g(){return Minecraft.getInstance();}
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
 static void pass(String s){results.add("PASS "+s);System.out.println("AIM PASS "+s);}
 static void command(String s){g().player.connection.sendCommand(s);}
 static Object menu(String s)throws Exception{var f=ClientMenu.class.getDeclaredField(s);f.setAccessible(true);return f.get(null);}
 static Object target()throws Exception{var f=AimLock.class.getDeclaredField("target");f.setAccessible(true);return f.get(null);}
 static void key(int scan,int code){var g=g();g.keyboardHandler.keyPress(g.getWindow().handle(),1,new KeyEvent(scan,code,0));g.keyboardHandler.keyPress(g.getWindow().handle(),0,new KeyEvent(scan,code,0));}
 static void click(int x,int y){var w=g().getWindow();float s=Math.min(1F,Math.min((w.getGuiScaledWidth()-20F)/460,(w.getGuiScaledHeight()-20F)/410));ClientMenu.click((w.getGuiScaledWidth()-460*s)/2+x*s,(w.getGuiScaledHeight()-410*s)/2+y*s);}
 static void shot(String s)throws Exception{Files.createDirectories(Path.of("/tmp/faster193-live/screenshots"));Screenshot.grab(new java.io.File("/tmp/faster193-live"),s,g().gameRenderer.mainRenderTarget(),1,c->{});}
 static RemotePlayer spawn(String name,int id,double dx,double dz){var p=new RemotePlayer(g().level,new com.mojang.authlib.GameProfile(UUID.nameUUIDFromBytes(name.getBytes()),name));p.setId(id);p.setPos(-255.5+dx,120,-255.5+dz);p.setOldPosAndRot();g().level.addEntity(p);return p;}
 static void move(RemotePlayer p,double dx,double dz){p.setPos(-255.5+dx,120,-255.5+dz);p.setOldPosAndRot();}
 static void look(float yaw,float pitch){g().player.setYRot(yaw);g().player.setXRot(pitch);}
 static double wrap(double v){v%=360;if(v>=180)v-=360;if(v< -180)v+=360;return v;}
 static void aimed(RemotePlayer p)throws Exception{check(target()==p,"unexpected locked target");check(aimFrames>0,"no actual camera-frame aiming observed");}
 public static void frame(Camera camera,float partial){
  try{var g=g();if(!expectTracking||camera!=g.gameRenderer.mainCamera()||!hold||!focus||!AimLockSettings.enabled()||g.player==null||g.gui.screen()!=null||g.gui.overlay()!=null||ClientMenu.isOpen()||FreeCam.active())return;
   var p=(net.minecraft.world.entity.player.Player)target();check(p!=null,"expected tracking frame has no target");
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
   if(t==80){key(InputConstants.KEY_ESCAPE,0);look(0,0);}

   if(t==100){check(target()==null&&Math.abs(g.player.getYRot())<.01,"aim changed without hold");pass("enabled module leaves manual view alone without hold");hold=true;expectTracking=true;}
   if(t==120){aimed(near);check(lastReadKey==InputConstants.KEY_R,"wrong hold key read");pass("acquires nearest visible head ahead within range");shot("initial-lock.png");move(far,0,2);}
   if(t==140){aimed(near);pass("a closer competitor cannot steal a living target");near.setPose(Pose.CROUCHING);near.refreshDimensions();}
   if(t==160){aimed(near);check(g.player.getXRot()>1,"crouched head height ignored");pass("sticky target follows crouching head height");near.setInvisible(true);expectTracking=false;look(44,13);}
   if(t==180){check(target()==near&&Math.abs(g.player.getYRot()-44)<.01,"invisibility switched target or kept aiming");pass("invisible target pauses aim while preserving identity");near.setInvisible(false);expectTracking=true;}
   if(t==200){aimed(near);pass("same target resumes after visibility returns");command("fill -255 120 -252 -254 123 -252 stone");expectTracking=false;}
   if(t==210){check(!g.player.hasLineOfSight(near),"fixture wall has not reached the client");look(55,10);}
   if(t==230){check(target()==near&&Math.abs(g.player.getYRot()-55)<.01&&!g.player.hasLineOfSight(near),"wall did not pause chosen target: target="+target()+" yaw="+g.player.getYRot()+" visible="+g.player.hasLineOfSight(near));pass("occlusion pauses aim without switching to clear competitor");shot("retained-occluded-target.png");command("fill -255 120 -252 -254 123 -252 air");}
   if(t==260){aimed(near);pass("original target resumes after wall clears");move(near,0,-5);expectTracking=true;}
   if(t==280){aimed(near);check(Math.abs(wrap(g.player.getYRot()-180))<.01,"living target lost behind initial view");pass("held target stays locked when moving behind original view");move(near,0,64);}
   if(t==300){aimed(near);pass("acquisition range does not break an existing living lock");near.setHealth(0);}
   if(t==320){aimed(far);pass("death ends original lock and permits a new nearby target");hold=false;expectTracking=false;look(77,12);}
   if(t==340){check(target()==null&&Math.abs(g.player.getYRot()-77)<.01,"release did not restore manual control");pass("key release clears target and immediately restores manual aiming");near.setHealth(20);move(near,0,3);move(far,0,-2);distant=spawn("AcrossMap",2100004,0,80);look(0,0);}
   if(t==360){hold=true;expectTracking=true;}
   if(t==380){aimed(near);pass("new press chooses nearby player ahead over closer player behind or across-map player");hold=false;expectTracking=false;look(0,0);move(near,0,50);}
   if(t==410){hold=true;}
   if(t==430){check(target()==null&&Math.abs(g.player.getYRot())<.01,"initial lock selected behind or distant player");pass("acquisition refuses behind-view and beyond-32-block players");move(near,0,3);expectTracking=true;}
   if(t==450){aimed(near);pass("held key can acquire when an eligible player enters view");key(InputConstants.KEY_P,'p');expectTracking=false;look(11,22);}
   if(t==470){check(target()==near&&Math.abs(g.player.getYRot()-11)<.01,"menu changed lock or view");pass("P menu pauses aim and preserves held target");key(InputConstants.KEY_ESCAPE,0);expectTracking=true;}
   if(t==490){aimed(near);FreeCam.enable();expectTracking=false;look(33,10);}
   if(t==510){check(target()==near&&Math.abs(g.player.getYRot()-33)<.01,"Free Cam changed body or lock");pass("Free Cam pauses body aiming and preserves held target");FreeCam.disable();expectTracking=true;}
   if(t==530){aimed(near);focus=false;expectTracking=false;look(55,0);}
   if(t==550){check(target()==near&&Math.abs(g.player.getYRot()-55)<.01,"focus loss changed lock or view");pass("focus loss pauses aim and preserves held target");focus=true;expectTracking=true;}
   if(t==570){aimed(near);AimLockSettings.setEnabled(false);expectTracking=false;look(66,15);}
   if(t==590){check(target()==null&&Math.abs(g.player.getYRot()-66)<.01,"disabled aim retained lock or changed view");pass("disabling module clears lock even while key is held");look(0,0);AimLockSettings.setEnabled(true);expectTracking=true;}
   if(t==600){aimed(near);near.discard();expectTracking=false;look(99,5);}
   if(t==610){check(target()==near&&Math.abs(g.player.getYRot()-99)<.01,"temporary removal switched to another player");pass("temporary entity removal pauses without switching identity");near=spawn("NearHead",2100005,0,3);expectTracking=true;}
   if(t==630){aimed(near);pass("replacement entity with same UUID resumes original player lock");near.setHealth(0);far.setHealth(0);distant.setHealth(0);expectTracking=false;look(88,0);}
   if(t==650){check(target()==null&&Math.abs(g.player.getYRot()-88)<.01,"stale dead target");pass("no living eligible players leaves view alone");check(rotations>0&&interactions==0,"look packet/attack audit");pass("vanilla look packets match visible angles; at most one movement packet per tick; zero attack interactions; lookPackets="+rotations+", alignedCameraFrames="+aimFrames);hold=false;AimLockSettings.setEnabled(false);AimLockSettings.setKey(InputConstants.KEY_R);Files.writeString(Path.of("/tmp/faster193-live/results.txt"),String.join("\n",results)+"\n");System.out.println("AIM ALL CHECKS COMPLETE");g.stop();}
  }catch(Exception e){throw new RuntimeException(e);}
 }
}
