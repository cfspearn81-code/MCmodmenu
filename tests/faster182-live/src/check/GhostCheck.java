package check;
import java.nio.file.*;
import java.util.*;
import net.fastersprint.*;
import net.fastersprint.mixin.ClientLevelPredictionAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
public final class GhostCheck {
 static final String[] CASES={"accepted","delay-accepted","reject","reject-release","rollback","secondary-ghost"};
 static boolean connecting,ready,releasing;public static boolean hold;static int warm=100,stage=-1,t,frame,attempts,ghosts,rollbacks,lastAttempt=-999;static double startZ,minY,maxZ;static int baseX;static boolean setup;
 static final List<String> results=new ArrayList<>();static final List<String> trace=new ArrayList<>();
 record Ack(ClientLevel level,int sequence,int due){} static final List<Ack> acks=new ArrayList<>();record Removal(BlockPos pos,int due){}static final List<Removal> removals=new ArrayList<>();
 static Minecraft g(){return Minecraft.getInstance();}static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
 static String name(){return stage>=0&&stage<CASES.length?CASES[stage]:"setup";}
 static Object field(String n)throws Exception{var f=AutoBridge.class.getDeclaredField(n);f.setAccessible(true);return f.get(null);}
 static void command(String s){g().player.connection.sendCommand(s);}
 static void stop(){var o=g().options;for(var k:new net.minecraft.client.KeyMapping[]{o.keyUp,o.keyDown,o.keyLeft,o.keyRight,o.keyJump,o.keySprint})k.setDown(false);}
 public static boolean delay(ClientLevel level,int seq){if(!releasing&&t>=30&&(name().equals("delay-accepted")||name().equals("reject-release"))){acks.add(new Ack(level,seq,frame+12));return true;}return false;}
 public static void interaction(BlockHitResult hit){if(t<30)return;int now=g().player.tickCount;check(now-lastAttempt>=4,"placement packet budget exceeded");lastAttempt=now;attempts++;BlockPos target=hit.getBlockPos().relative(hit.getDirection());if(!g().level.getBlockState(target).getCollisionShape(g().level,target).isEmpty())ghosts++;if(name().equals("reject-release"))hold=false;}
 public static void confirmation(){if(!name().equals("rollback")||t<30)return;try{if((boolean)field("acknowledged")){var pos=(BlockPos)field("pending");if(pos!=null&&removals.stream().noneMatch(r->r.pos.equals(pos)))removals.add(new Removal(pos.immutable(),frame+1));}}catch(Exception e){throw new RuntimeException(e);}}
 static void begin()throws Exception{stop();hold=false;stage++;t=0;attempts=ghosts=rollbacks=0;lastAttempt=-999;removals.clear();setup=true;
  if(stage==CASES.length){finish();return;}
  baseX=-128-stage*16;
  command("gamemode "+(name().startsWith("reject")?"adventure":"survival")+" BridgeLive");
  command("fill "+(baseX-8)+" 119 -136 "+(baseX+8)+" 126 -96 air");command("fill "+(baseX-2)+" 119 -130 "+(baseX+2)+" 119 -126 stone");command("clear BridgeLive");if(!name().equals("secondary-ghost"))command("give BridgeLive stone 64");command("tp BridgeLive "+(baseX+.5)+" 120 -127.5 180 88");
  System.out.println("GHOST START "+name());
 }
 public static void tick(){var g=g();frame++;
  if(g.player==null||g.level==null){if(!connecting&&g.gui.overlay()==null){connecting=true;net.minecraft.client.gui.screens.ConnectScreen.startConnecting(g.gui.screen(),g,new net.minecraft.client.multiplayer.resolver.ServerAddress("127.0.0.1",25568),new net.minecraft.client.multiplayer.ServerData("Ghost safety regression","127.0.0.1:25568",net.minecraft.client.multiplayer.ServerData.Type.OTHER),false,null);}return;}
  try{
   g.options.pauseOnLostFocus=false;g.gui.toastManager().clear();
   if(!ready){ready=true;g.options.autoJump().set(false);PlayerDisplaySettings.setAutoBridgeEnabled(true);PlayerDisplaySettings.setAutoHitEnabled(false);PlayerDisplaySettings.setEnabled(false);PlayerDisplaySettings.setGuiMoveEnabled(false);XraySettings.setEnabled(false);BedEspSettings.setEnabled(false);TintSettings.SKY.setEnabled(false);QuickJumpSettings.setEnabled(false);QuickFallSettings.setEnabled(false);WallClimbSettings.setEnabled(false);FreeCam.disable();command("gamemode survival BridgeLive");command("effect give BridgeLive resistance infinite 4 true");command("tp BridgeLive -64.5 81 -64.5 0 0");command("forceload add -224 -144 -112 -96");}
   for(var it=acks.iterator();it.hasNext();){var a=it.next();if(a.due<=frame){releasing=true;a.level.handleBlockChangedAck(a.sequence);releasing=false;it.remove();}}
   for(var it=removals.iterator();it.hasNext();){var r=it.next();if(r.due<=frame){command("setblock "+r.pos.getX()+" "+r.pos.getY()+" "+r.pos.getZ()+" air");rollbacks++;it.remove();}}
   if(warm-->0)return;if(stage==-1){stage=Integer.getInteger("ghost.start",0)-1;begin();return;}if(stage>=CASES.length)return;t++;
   if(name().startsWith("reject"))g.gameMode.setLocalMode(GameType.SURVIVAL); // Deliberate fixture-only client/server mismatch produces real rejected predictions.
   if(t==30){hold=true;startZ=g.player.getZ();minY=g.player.getY();maxZ=startZ;check(g.player.onGround()&&Math.abs(minY-120)<.01,"scene not ready "+g.player.position());
    if(name().equals("secondary-ghost")){var prediction=((ClientLevelPredictionAccessor)g.level).fastersprint$predictionHandler();try(var ignored=prediction.startPredicting()){for(int x=baseX+3;x<=baseX+5;x++)for(int z=-125;z<=-123;z++){var pos=new BlockPos(x,119,z);g.level.setBlock(pos,Blocks.STONE.defaultBlockState(),3);ghosts++;}}
     var probe=g.player.getBoundingBox().move(3,0,3);var checkSupport=AutoBridge.class.getDeclaredMethod("supported",Minecraft.class,net.minecraft.world.phys.AABB.class,double.class);checkSupport.setAccessible(true);
     check(!g.level.getBlockState(BlockPos.containing(baseX+3,119,-125)).getCollisionShape(g.level,new BlockPos(baseX+3,119,-125)).isEmpty(),"secondary ghost not visibly solid");
     check(!(boolean)checkSupport.invoke(null,g,probe,.08),"unconfirmed secondary block trusted as footing");
    }
    g.options.keyDown.setDown(true);if(name().equals("secondary-ghost"))g.options.keyRight.setDown(true);
   }
   if(t>=30){
    g.player.setYRot(180);float pitch=88;
    if(!name().equals("secondary-ghost")){var under=BlockPos.containing(g.player.getX(),Math.floor(g.player.getY())-1,g.player.getZ());if(g.level.getBlockState(under).canBeReplaced()){var support=under.north();if(!g.level.getBlockState(support).canBeReplaced()){double d=g.player.getZ()-(support.getZ()+1.0);if(d>.001)pitch=(float)Math.toDegrees(Math.atan2(g.player.getEyePosition().y-(support.getY()+.5),d));}}}
    g.player.setXRot(pitch);minY=Math.min(minY,g.player.getY());maxZ=Math.max(maxZ,g.player.getZ());trace.add(name()+","+t+","+g.player.getX()+","+g.player.getY()+","+g.player.getZ()+","+AutoBridge.status());check(minY>=119.99,"fell on "+name()+" "+g.player.position()+" status="+AutoBridge.status());
    if(t==210){stop();hold=false;
     if(name().endsWith("accepted"))check(attempts>=2&&maxZ-startZ>4,"confirmed bridge failed to progress "+(maxZ-startZ)+" attempts="+attempts);
     if(name().startsWith("reject")){check(attempts>0&&ghosts>0,"rejection case never predicted a block");check(maxZ< -124.78,"walked onto rejected footing "+maxZ);if(name().equals("reject-release"))check(attempts==1,"resent while bridge key released");}
     if(name().equals("rollback")){check(rollbacks>0,"no late rollbacks tested");check(maxZ< -124.78,"entered rolled-back footing "+maxZ);check(attempts<=3,"rollback rejection backoff failed");}
     if(name().equals("secondary-ghost"))check(maxZ< -124.78,"trusted a secondary prediction");
     String line="PASS "+name()+": attempts="+attempts+", predictedBlocks="+ghosts+", rollbackRemovals="+rollbacks+", travelZ="+(maxZ-startZ)+", minY="+minY;results.add(line);System.out.println("GHOST "+line);
    }
    if(t==230){if(name().equals("reject-release"))check(!AutoBridge.isProtecting(),"guard latch did not release after stopping");begin();}
   }
  }catch(Exception e){throw new RuntimeException(e);}
 }
 static void finish()throws Exception{stop();hold=false;PlayerDisplaySettings.setAutoBridgeEnabled(false);command("gamemode survival BridgeLive");Files.writeString(Path.of("/tmp/faster182-live/results.txt"),String.join("\n",results)+"\n");Files.writeString(Path.of("/tmp/faster182-live/trace.csv"),"case,tick,x,y,z,status\n"+String.join("\n",trace)+"\n");System.out.println("GHOST ALL CHECKS COMPLETE");g().stop();}
}
