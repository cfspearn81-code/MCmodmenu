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
 static final String[] CASES={"accepted","delay-accepted","reject","reject-release","rollback","late-rollback","ack-only-ghost","delayed-block-update","secondary-ghost","secondary-ack-only-jump","fast-then-reject","fast-ack-only-ghost","fast-then-rollback"};
 static boolean connecting,ready,releasing;public static boolean hold;static int warm=100,stage=-1,t,frame,attempts,ghosts,rollbacks,lastAttempt=-999;static double startZ,minY,maxZ;static int baseX;static boolean setup;
 static final List<BlockPos> confirmedHistory=new ArrayList<>();
 static int fastTicks,faultStart=-1,attemptsAtFault,rejectionFeedback;
 static int suppressed,proofs;static double maxY;
 static final List<String> results=new ArrayList<>();static final List<String> trace=new ArrayList<>();
 record Update(ClientLevel level,BlockPos pos,net.minecraft.world.level.block.state.BlockState state,int flags,int due){}static final List<Update> updates=new ArrayList<>();public static boolean delivering;
 record Ack(ClientLevel level,int sequence,int due){} static final List<Ack> acks=new ArrayList<>();record Removal(BlockPos pos,int due){}static final List<Removal> removals=new ArrayList<>();
 static Minecraft g(){return Minecraft.getInstance();}static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
 static String name(){return stage>=0&&stage<CASES.length?CASES[stage]:"setup";}
 static int settling()throws Exception{var p=(BridgeConfirmationPolicy)field("confirmations");return p.settleTicks();}
 static Object field(String n)throws Exception{var f=AutoBridge.class.getDeclaredField(n);f.setAccessible(true);return f.get(null);}
 static void command(String s){g().player.connection.sendCommand(s);}
 static void stop(){var o=g().options;for(var k:new net.minecraft.client.KeyMapping[]{o.keyUp,o.keyDown,o.keyLeft,o.keyRight,o.keyJump,o.keySprint})k.setDown(false);}
 public static boolean delay(ClientLevel level,int seq){
  if(t>=30&&(name().equals("ack-only-ghost")||name().equals("delayed-block-update")||(name().equals("fast-ack-only-ghost")&&faultStart>=0)))try{
   var p=(BlockPos)field("pending");if(p!=null&&!g().level.getBlockState(p).isAir()){
    // Simulate an ACK-only protocol/proxy path retaining a visible prediction without a block-state packet.
    ((ClientLevelPredictionAccessor)level).fastersprint$predictionHandler().updateKnownServerState(p,level.getBlockState(p));proofs++;
   }
  }catch(Exception e){throw new RuntimeException(e);}
if(!releasing&&t>=30&&(name().equals("delay-accepted")||name().equals("reject-release"))){acks.add(new Ack(level,seq,frame+12));return true;}return false;}
 public static void interaction(BlockHitResult hit){if(t<30)return;int now=g().player.tickCount;check(now-lastAttempt>=4,"placement packet budget exceeded");lastAttempt=now;attempts++;BlockPos target=hit.getBlockPos().relative(hit.getDirection());if(!g().level.getBlockState(target).getCollisionShape(g().level,target).isEmpty())ghosts++;if(name().equals("reject-release"))hold=false;}
 public static void confirmation(){try{if((boolean)field("acknowledged")){var p=(BlockPos)field("pending");if(p!=null&&!confirmedHistory.contains(p))confirmedHistory.add(p.immutable());}}catch(Exception e){throw new RuntimeException(e);}
 if((!name().equals("rollback")&&!name().equals("late-rollback"))||t<30)return;try{if((boolean)field("acknowledged")){var pos=(BlockPos)field("pending");if(pos!=null&&removals.stream().noneMatch(r->r.pos.equals(pos)))removals.add(new Removal(pos.immutable(),frame+(name().equals("late-rollback")?6:1)));}}catch(Exception e){throw new RuntimeException(e);}}
 public static boolean serverUpdate(ClientLevel level,BlockPos pos,net.minecraft.world.level.block.state.BlockState state,int flags){
  if(name().equals("fast-then-reject")&&faultStart>=0&&state.isAir()&&!level.getBlockState(pos).isAir())rejectionFeedback++;
  if(delivering||t<30||(!name().equals("ack-only-ghost")&&!name().equals("delayed-block-update")&&!(name().equals("fast-ack-only-ghost")&&faultStart>=0)))return false;
  try{if(!pos.equals(field("pending")))return false;}catch(Exception e){throw new RuntimeException(e);}
  suppressed++;if(name().equals("delayed-block-update"))updates.add(new Update(level,pos.immutable(),state,flags,frame+40));return true;
 }
 static void begin()throws Exception{stop();hold=false;stage++;t=0;attempts=ghosts=rollbacks=suppressed=proofs=fastTicks=rejectionFeedback=0;faultStart=-1;attemptsAtFault=0;lastAttempt=-999;removals.clear();updates.clear();confirmedHistory.clear();setup=true;
  if(stage==CASES.length){finish();return;}
  // Each case starts with a fresh actor context, as on reconnect, without carrying prior unresolved attempts.
  var actor=AutoBridge.class.getDeclaredField("actor");actor.setAccessible(true);actor.set(null,null);
  baseX=-128-stage*16;
  command("gamemode "+((name().startsWith("reject")||name().equals("ack-only-ghost"))?"adventure":"survival")+" BridgeLive");
  command("fill "+(baseX-8)+" 119 -136 "+(baseX+8)+" 126 -96 air");command("fill "+(baseX-2)+" 119 -130 "+(baseX+2)+" 119 -126 stone");command("clear BridgeLive");if(!name().startsWith("secondary-"))command("give BridgeLive stone 64");command("tp BridgeLive "+(baseX+.5)+" 120 -127.5 180 88");
  System.out.println("GHOST START "+name());
 }
 public static void tick(){var g=g();frame++;
  if(g.player==null||g.level==null){if(!connecting&&g.gui.overlay()==null){connecting=true;net.minecraft.client.gui.screens.ConnectScreen.startConnecting(g.gui.screen(),g,new net.minecraft.client.multiplayer.resolver.ServerAddress("127.0.0.1",25568),new net.minecraft.client.multiplayer.ServerData("Ghost safety regression","127.0.0.1:25568",net.minecraft.client.multiplayer.ServerData.Type.OTHER),false,null);}return;}
  try{
   g.options.pauseOnLostFocus=false;g.gui.toastManager().clear();
   if(!ready){ready=true;g.options.autoJump().set(false);PlayerDisplaySettings.setAutoBridgeEnabled(true);PlayerDisplaySettings.setAutoHitEnabled(false);PlayerDisplaySettings.setEnabled(false);PlayerDisplaySettings.setGuiMoveEnabled(false);XraySettings.setEnabled(false);BedEspSettings.setEnabled(false);TintSettings.SKY.setEnabled(false);QuickJumpSettings.setEnabled(false);QuickFallSettings.setEnabled(false);WallClimbSettings.setEnabled(false);FreeCam.disable();command("gamemode survival BridgeLive");command("effect give BridgeLive resistance infinite 4 true");command("tp BridgeLive -64.5 81 -64.5 0 0");command("forceload add -352 -144 -112 -96");}
   for(var it=updates.iterator();it.hasNext();){var u=it.next();if(u.due<=frame){delivering=true;u.level.setServerVerifiedBlockState(u.pos,u.state,u.flags);delivering=false;it.remove();}}
   for(var it=acks.iterator();it.hasNext();){var a=it.next();if(a.due<=frame){releasing=true;a.level.handleBlockChangedAck(a.sequence);releasing=false;it.remove();}}
   for(var it=removals.iterator();it.hasNext();){var r=it.next();if(r.due<=frame){command("setblock "+r.pos.getX()+" "+r.pos.getY()+" "+r.pos.getZ()+" air");rollbacks++;it.remove();}}
   if(warm-->0)return;if(stage==-1){stage=Integer.getInteger("ghost.start",0)-1;begin();return;}if(stage>=CASES.length)return;t++;
   if(name().startsWith("reject")||name().equals("ack-only-ghost"))g.gameMode.setLocalMode(GameType.SURVIVAL); // Deliberate fixture-only client/server mismatch produces real rejected predictions.
   if(t>=30&&settling()==1)fastTicks++;
   if(name().startsWith("fast-")&&faultStart<0&&t>=100&&settling()==1&&attempts>=5){
    faultStart=t;attemptsAtFault=attempts;
    if(name().equals("fast-then-rollback")){var p=confirmedHistory.getFirst();check(p.distToCenterSqr(g.player.position())>2.25,"rollback probe too close to actor");command("setblock "+p.getX()+" "+p.getY()+" "+p.getZ()+" air");rollbacks++;}
    else command("gamemode adventure BridgeLive");
   }
   if((name().equals("fast-then-reject")||name().equals("fast-ack-only-ghost"))&&faultStart>=0)g.gameMode.setLocalMode(GameType.SURVIVAL);
   if(t==30){hold=true;startZ=g.player.getZ();minY=maxY=g.player.getY();maxZ=startZ;check(g.player.onGround()&&Math.abs(minY-120)<.01,"scene not ready "+g.player.position());
    if(name().startsWith("secondary-")){var prediction=((ClientLevelPredictionAccessor)g.level).fastersprint$predictionHandler();try(var ignored=prediction.startPredicting()){for(int x=baseX+3;x<=baseX+5;x++)for(int z=-125;z<=-123;z++){var pos=new BlockPos(x,119,z);g.level.setBlock(pos,Blocks.STONE.defaultBlockState(),3);ghosts++;}}
     if(name().equals("secondary-ack-only-jump")){
      for(int x=baseX+3;x<=baseX+5;x++)for(int z=-125;z<=-123;z++)prediction.updateKnownServerState(new BlockPos(x,119,z),Blocks.STONE.defaultBlockState());
      g.level.handleBlockChangedAck(prediction.currentSequence());proofs++;
     }
     var probe=g.player.getBoundingBox().move(3,0,3);var checkSupport=AutoBridge.class.getDeclaredMethod("supported",Minecraft.class,net.minecraft.world.phys.AABB.class,double.class);checkSupport.setAccessible(true);
     check(!g.level.getBlockState(BlockPos.containing(baseX+3,119,-125)).getCollisionShape(g.level,new BlockPos(baseX+3,119,-125)).isEmpty(),"secondary ghost not visibly solid");
     check(!(boolean)checkSupport.invoke(null,g,probe,.08),"unconfirmed secondary block trusted as footing");
    }
    g.options.keyDown.setDown(true);if(name().startsWith("secondary-"))g.options.keyRight.setDown(true);if(name().equals("secondary-ack-only-jump"))g.options.keyJump.setDown(true);
   }
   if(t==70&&name().equals("secondary-ack-only-jump"))hold=false;
   if(t>=30){
    g.player.setYRot(180);float pitch=88;
    if(!name().startsWith("secondary-")){var under=BlockPos.containing(g.player.getX(),Math.floor(g.player.getY())-1,g.player.getZ());if(g.level.getBlockState(under).canBeReplaced()){var support=under.north();if(!g.level.getBlockState(support).canBeReplaced()){double d=g.player.getZ()-(support.getZ()+1.0);if(d>.001)pitch=(float)Math.toDegrees(Math.atan2(g.player.getEyePosition().y-(support.getY()+.5),d));}}}
    g.player.setXRot(pitch);minY=Math.min(minY,g.player.getY());maxY=Math.max(maxY,g.player.getY());maxZ=Math.max(maxZ,g.player.getZ());trace.add(name()+","+t+","+g.player.getX()+","+g.player.getY()+","+g.player.getZ()+","+AutoBridge.status());check(minY>=119.99,"fell on "+name()+" "+g.player.position()+" status="+AutoBridge.status());
    if(t==210){if(name().equals("secondary-ack-only-jump"))check(AutoBridge.isProtecting(),"manual prediction guard dropped on key release while moving");stop();hold=false;
     if(name().endsWith("accepted"))check(attempts>=2&&maxZ-startZ>4,"confirmed bridge failed to progress "+(maxZ-startZ)+" attempts="+attempts);
     if(name().startsWith("reject")){check(attempts>0&&ghosts>0,"rejection case never predicted a block");check(maxZ< -124.78,"walked onto rejected footing "+maxZ);if(name().equals("reject-release"))check(attempts==1,"resent while bridge key released");}
     if(name().equals("rollback")||name().equals("late-rollback")){check(rollbacks>0,"no late rollbacks tested");check(maxZ< -124.78,"entered rolled-back footing "+maxZ);check(attempts<=3,"rollback rejection backoff failed");}
     if(name().startsWith("secondary-"))check(maxZ< -124.78,"trusted a secondary prediction");
     if(name().equals("ack-only-ghost")){check(attempts==1&&ghosts>0&&suppressed>0&&proofs>0,"ACK-only case did not exercise a retained ghost");check(field("pending")!=null&&(boolean)field("acknowledged"),"ACK-only ghost never reached reconciled pending state");check(maxZ< -124.78,"walked onto ACK-only ghost");check(AutoBridge.status().equals("Unconfirmed: stopped safely"),"unconfirmed timeout did not stop safely");}
     if(name().equals("delayed-block-update"))check(suppressed>0&&proofs>0&&maxZ-startZ>1,"delayed explicit updates did not resume bridge");
     if(name().equals("secondary-ack-only-jump"))check(proofs>0&&maxY>121,"jump/ACK-only secondary scenario not exercised");
     if(name().equals("accepted")){check(fastTicks>30,"clean server never earned fast mode");check(maxZ-startZ>17,"fast bridging failed to beat the 1.9.1 baseline by at least 30%");}
     if(name().equals("fast-then-reject")){check(faultStart>0&&attemptsAtFault>=5&&fastTicks>0,"fast rejection case never earned fast mode");check(rejectionFeedback>0&&attempts>attemptsAtFault,"no actual failed server placement after fast mode");check(settling()==10,"rejection did not restore conservative confirmation");check(attempts-attemptsAtFault<=4,"post-rejection attempt budget exceeded");}
     if(name().equals("fast-ack-only-ghost")){check(faultStart>0&&fastTicks>0&&proofs>0&&suppressed>0,"fast ACK-only fault not exercised");check(field("pending")!=null&&(boolean)field("acknowledged"),"fast mode accepted an unconfirmed ghost");check(attempts-attemptsAtFault<=2,"fast ghost caused resends");}
     if(name().equals("fast-then-rollback"))check(faultStart>0&&fastTicks>0&&rollbacks==1&&settling()==10,"recent approved footing rollback did not restore conservative checks");
     String line="PASS "+name()+": attempts="+attempts+", predictedBlocks="+ghosts+", rollbackRemovals="+rollbacks+", travelZ="+(maxZ-startZ)+", minY="+minY+", maxY="+maxY+", suppressedUpdates="+suppressed+", ackOnlyProofs="+proofs+", fastTicks="+fastTicks+", faultStart="+faultStart+", rejectionFeedback="+rejectionFeedback;results.add(line);System.out.println("GHOST "+line);
    }
    if(t==230){if(name().equals("reject-release"))check(!AutoBridge.isProtecting(),"guard latch did not release after stopping");begin();}
   }
  }catch(Exception e){throw new RuntimeException(e);}
 }
 static void finish()throws Exception{stop();hold=false;PlayerDisplaySettings.setAutoBridgeEnabled(false);command("gamemode survival BridgeLive");Files.writeString(Path.of("/tmp/faster192-live/results.txt"),String.join("\n",results)+"\n");Files.writeString(Path.of("/tmp/faster192-live/trace.csv"),"case,tick,x,y,z,status\n"+String.join("\n",trace)+"\n");System.out.println("GHOST ALL CHECKS COMPLETE");g().stop();}
}
