package check;
import check.mixin.AttackInvoker;
import com.mojang.blaze3d.platform.InputConstants;
import java.nio.file.*;
import java.util.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import net.spawnnpc.*;
public final class NpcCheck {
 public static boolean integration=FabricLoader.getInstance().isModLoaded("fastersprint"),aimHold;
 static boolean connecting,ready,finished;static int warm=100,t,fakeAttacks,fakeInteractions,spawnPackets,ordinaryAttacks,ordinaryCommands;
 static TestNpc first,second,kb,fixed;static Vec3 stationaryStart,kbStart,fixedStart;static double maxY,maxZ;static ClientLevel oldWorld;static final List<TestNpc> oldNpcs=new ArrayList<>();static final Set<Integer> ids=new HashSet<>();static final List<String> results=new ArrayList<>();
 static Minecraft g(){return Minecraft.getInstance();}static void check(boolean b,String m){if(!b)throw new AssertionError(m);}static void pass(String m){results.add("PASS "+m);System.out.println("NPC PASS "+m);}
 static List<TestNpc> npcs(){return g().level.players().stream().filter(p->p instanceof TestNpc).map(p->(TestNpc)p).toList();}
 static void command(String s){g().player.connection.sendCommand(s);}
 static void chat(String s){var screen=new ChatScreen(s,false);g().gui.setScreen(screen);screen.keyPressed(new KeyEvent(InputConstants.KEY_RETURN,13,0));g().gui.setScreen(null);}
 static void item(Item item){g().player.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(item,5));}
 static void click(TestNpc npc){g().hitResult=new EntityHitResult(npc,npc.getEyePosition());((AttackInvoker)g()).check$attack();}
 static Object aimTarget()throws Exception{var f=Class.forName("net.fastersprint.AimLock").getDeclaredField("target");f.setAccessible(true);return f.get(null);}
 static void enabled(String className,String method,boolean value)throws Exception{Class.forName("net.fastersprint."+className).getMethod(method,boolean.class).invoke(null,value);}
 static void configureFaster()throws Exception{
  if(!integration)return;enabled("PlayerDisplaySettings","setAutoBridgeEnabled",false);enabled("PlayerDisplaySettings","setAutoHitEnabled",false);enabled("PlayerDisplaySettings","setEnabled",false);enabled("PlayerDisplaySettings","setGuiMoveEnabled",false);
  for(String cls:new String[]{"XraySettings","BedEspSettings","QuickFallSettings","QuickJumpSettings","WallClimbSettings","AimLockSettings"})enabled(cls,"setEnabled",false);
  Class.forName("net.fastersprint.FreeCam").getMethod("disable").invoke(null);
  Class.forName("net.fastersprint.AimLockSettings").getMethod("setKey",int.class).invoke(null,InputConstants.KEY_R);
 }
 public static void packet(Packet<?> p){
  if(p instanceof ServerboundAttackPacket a){if(ids.contains(a.entityId()))fakeAttacks++;else ordinaryAttacks++;}
  if(p instanceof ServerboundInteractPacket i&&ids.contains(i.entityId()))fakeInteractions++;
  if(p instanceof ServerboundChatCommandPacket c){if(c.command().strip().split("\\s+",2)[0].equals("spawnnpc"))spawnPackets++;else ordinaryCommands++;}
 }
 public static void tick(){if(finished)return;var g=g();if(g.player==null||g.level==null){if(!connecting&&g.gui.overlay()==null){connecting=true;net.minecraft.client.gui.screens.ConnectScreen.startConnecting(g.gui.screen(),g,new net.minecraft.client.multiplayer.resolver.ServerAddress("127.0.0.1",25568),new net.minecraft.client.multiplayer.ServerData("Spawn NPC test","127.0.0.1:25568",net.minecraft.client.multiplayer.ServerData.Type.OTHER),false,null);}return;}
  try{g.options.pauseOnLostFocus=false;g.gui.toastManager().clear();
   if(!ready){ready=true;configureFaster();g.options.guiScale().set(2);g.options.renderDistance().set(3);command("gamemode creative BridgeLive");command("execute in minecraft:overworld run forceload add -272 -272 -240 -240");command("execute in minecraft:overworld run tp BridgeLive -255.5 120 -255.5 0 0");command("time set 6000");command("weather clear");}
   if(warm-->0||g.gui.overlay()!=null)return;t++;
   if(t==1){command("fill -264 119 -264 -246 119 -246 stone");command("fill -264 120 -264 -246 125 -246 air");command("tp BridgeLive -255.5 120 -255.5 0 0");}
   if(t==2&&Boolean.getBoolean("npc.kbOnly"))t=144;
   if(t==25){item(Items.DIAMOND_SWORD);g.player.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.DIAMOND_HELMET));chat("/spawnnpc");check(npcs().size()==1,"chat Enter did not spawn one NPC");first=npcs().getFirst();ids.add(first.getId());check(!first.hasKnockback()&&first.isNoGravity(),"plain command changed default mode");pass("chat Enter /spawnnpc creates a standalone local player entity (Faster loaded="+integration+")");
    check(first.getId()<0&&!first.getUUID().equals(g.player.getUUID())&&g.level.getEntity(first.getId())==first,"identity/id ownership");check(first.getSkin().equals(g.player.getSkin())&&first.getMainArm()==g.player.getMainArm(),"skin/model/hand mismatch");
    for(var slot:new EquipmentSlot[]{EquipmentSlot.MAINHAND,EquipmentSlot.HEAD})check(ItemStack.isSameItemSameComponents(first.getItemBySlot(slot),g.player.getItemBySlot(slot))&&first.getItemBySlot(slot)!=g.player.getItemBySlot(slot),"equipment is not independently copied");
    check(g.level.noCollision(first,first.getBoundingBox())&&!g.level.noCollision(first,first.getBoundingBox().move(0,-.05,0)),"NPC is not standing on clear floor");pass("unique local ID/UUID, matching player skin and independent equipment copies on clear ground");}
   if(t==35){Files.createDirectories(Path.of("/tmp/spawnnpc110-live/screenshots"));Screenshot.grab(new java.io.File("/tmp/spawnnpc110-live"),(integration?"integration":"standalone")+"-npc.png",g.gameRenderer.mainRenderTarget(),1,c->{});}
   if(t==40){Vec3 before=stationaryStart=first.position();item(Items.STICK);click(first);check(!first.isRemoved()&&first.isAlive()&&first.hurtTime>0&&first.position().equals(before),"ordinary hit removed or moved dummy");check(g.player.getAttackStrengthScale(0)<.1,"local hit did not use cooldown");pass("ordinary left click gives local hurt feedback/cooldown and preserves stationary dummy");}
   if(t==50){g.player.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.DIAMOND_BLOCK));g.gameMode.attack(g.player,first);check(!first.isRemoved(),"offhand diamond block removed NPC with other mainhand item");pass("diamond block in offhand alone does not remove the dummy");g.player.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);
    g.gameMode.interact(g.player,first,new EntityHitResult(first,first.getEyePosition()),InteractionHand.MAIN_HAND);check(fakeInteractions==0,"right click leaked fake entity interaction");pass("right clicks on local dummies do not send invalid server entity interactions");}
   if(t==60){if(integration){enabled("AimLockSettings","setEnabled",true);aimHold=true;g.player.setYRot(0);g.player.setXRot(0);}else check(!FabricLoader.getInstance().isModLoaded("fastersprint"),"standalone test contains Faster");}
   if(t==70){check(first.position().distanceToSqr(stationaryStart)<1e-12,"default dummy drifted after ordinary hits");pass("plain /spawnnpc remains stationary across later physics ticks");}
   if(t==80){if(integration){check(aimTarget()==first,"Faster cannot acquire NPC as a player");pass("Faster Aim Lock acquires standalone NPC as a normal player target");aimHold=false;enabled("AimLockSettings","setEnabled",false);}
    item(Items.DIAMOND_BLOCK);int count=g.player.getMainHandItem().getCount();click(first);check(first.isRemoved()&&g.level.getEntity(first.getId())==null&&!g.level.players().contains(first)&&npcs().isEmpty(),"diamond left click did not fully despawn NPC");check(g.player.getMainHandItem().getCount()==count,"diamond item was consumed");pass("diamond-block mainhand left click fully despawns dummy without consuming block");
    g.gameMode.attack(g.player,first);g.gameMode.interact(g.player,first,new EntityHitResult(first,first.getEyePosition()),InteractionHand.MAIN_HAND);
    check(fakeAttacks==0&&fakeInteractions==0,"rapid clicks on removed dummy leaked packets");pass("repeat clicks after despawn stay local without touching other entities");}
   if(t==100){item(Items.STICK);chat("/spawnnpc");first=npcs().getFirst();ids.add(first.getId());chat("/spawnnpc");check(npcs().size()==2,"multiple spawn commands failed");second=npcs().stream().filter(p->p!=first).findFirst().orElseThrow();ids.add(second.getId());check(!first.getUUID().equals(second.getUUID())&&!first.getBoundingBox().intersects(second.getBoundingBox()),"multiple NPCs overlap or share identity");pass("repeated commands create independent non-overlapping player dummies");item(Items.DIAMOND_BLOCK);g.gameMode.attack(g.player,first);check(first.isRemoved()&&NpcManager.owns(second)&&!second.isRemoved()&&npcs().size()==1,"removing one dummy affected another");pass("diamond block removes only the targeted owned dummy");}
   if(t==110){int size=npcs().size();chat("/spawnnpc extra");chat("/spawnnpc kb extra");chat("/spawnnpc KB");check(npcs().size()==size,"extra arguments unexpectedly spawned NPC");check(!NpcManager.command("spawnnpc_other")&&!NpcManager.command("say spawnnpc"),"other commands intercepted");int before=ordinaryCommands;command("help");check(ordinaryCommands>before,"normal server commands stopped forwarding");pass("invalid arguments show usage; unrelated server commands still pass through");command("summon minecraft:armor_stand -255.5 120 -257.5");}
   if(t==115){chat("/spawnnpc");var stale=npcs().stream().filter(p->p!=second).findFirst().orElseThrow();ids.add(stale.getId());
    var replacement=new net.minecraft.client.player.RemotePlayer(g.level,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"Replacement"));replacement.setId(stale.getId());replacement.setPos(stale.position());replacement.setOldPosAndRot();g.level.addEntity(replacement);
    check(!NpcManager.owns(stale)&&!NpcManager.owns(replacement),"reused ID still considered mod-owned");check(NpcManager.attack(g.player,stale)&&g.level.getEntity(replacement.getId())==replacement,"stale ownership removed replacement entity");
    g.level.removeEntity(replacement.getId(),Entity.RemovalReason.DISCARDED);pass("reused entity ID cannot let a stale dummy reference remove another entity");}
   if(t==130){Entity stand=null;for(Entity e:g.level.entitiesForRendering())if(e instanceof ArmorStand){stand=e;break;}check(stand!=null,"server armor stand missing");check(!NpcManager.owns(stand),"server entity wrongly owned");int before=ordinaryAttacks;g.gameMode.attack(g.player,stand);check(ordinaryAttacks==before+1,"normal server entity attack was blocked");pass("normal server entities retain ordinary attack behavior even with diamond block");}
   if(t==145){item(Items.DIAMOND_BLOCK);for(var p:new ArrayList<>(npcs()))g.gameMode.attack(g.player,p);check(npcs().isEmpty(),"pre-KB cleanup failed");g.player.setYRot(0);g.player.setXRot(0);}
   if(t==150){item(Items.STICK);chat("/spawnnpc    kb   ");check(npcs().size()==1,"kb command did not spawn one dummy");kb=npcs().getFirst();ids.add(kb.getId());check(kb.hasKnockback()&&!kb.isNoGravity()&&!kb.noPhysics,"kb mode flags");kbStart=kb.position();
    g.player.setYRot(180);chat("/spawnnpc");fixed=npcs().stream().filter(p->p!=kb).findFirst().orElseThrow();ids.add(fixed.getId());fixedStart=fixed.position();check(!fixed.hasKnockback()&&fixed.isNoGravity(),"kb setting leaked to later normal spawn");g.player.setYRot(0);pass("/spawnnpc kb enables physics only on that NPC, including normalized command whitespace");}
   if(t==170){check(kb.position().distanceToSqr(kbStart)<1e-8&&kb.onGround(),"unhit kb dummy drifted or failed to settle");maxY=kb.getY();maxZ=kb.getZ();item(Items.STICK);click(kb);check(kb.getDeltaMovement().z>.35&&kb.getDeltaMovement().y>.35,"ordinary KB hit did not produce native outward/upward impulse: velocity="+kb.getDeltaMovement()+" hurt="+kb.hurtTime+" onGround="+kb.onGround()+" npc="+kb.position()+" player="+g.player.position());pass("ordinary hit applies outward knockback and grounded upward hop to kb dummy");}
   if(t>=170&&t<=210){maxY=Math.max(maxY,kb.getY());maxZ=Math.max(maxZ,kb.getZ());check(g.level.noCollision(kb,kb.getBoundingBox()),"kb dummy penetrated terrain");}
   if(t==190){check(maxZ-kbStart.z>.5&&maxY-kbStart.y>.2,"kb dummy did not actually travel after hit");g.gameMode.attack(g.player,fixed);pass("kb dummy visibly moves away and rises over subsequent ticks");}
   if(t==210){check(kb.onGround()&&Math.abs(kb.getY()-kbStart.y)<.001&&kb.getDeltaMovement().horizontalDistanceSqr()<.0001,"kb dummy did not land/slow down "+kb.position()+" "+kb.getDeltaMovement());check(kb.getHealth()==20,"test knockback dealt damage");check(fixed.position().distanceToSqr(fixedStart)<1e-12,"normal dummy moved while kb dummy was active");pass("native gravity/drag lands kb dummy on floor while normal dummy remains fixed");command("tp BridgeLive "+kb.getX()+" 120 "+(kb.getZ()-2.5)+" 0 0");}
   if(t==225&&integration){enabled("AimLockSettings","setEnabled",true);aimHold=true;}
   if(t==230){item(Items.STICK);click(kb);check(kb.getDeltaMovement().z>.35&&kb.getDeltaMovement().y>.35,"settled kb dummy did not react to repeat hit");pass("landed kb dummy reacts to a second hit");}
   if(t==232){if(integration){check(aimTarget()==kb,"Faster lost moving kb target");pass("Faster Aim Lock recognizes and retains moving knockback dummy");aimHold=false;enabled("AimLockSettings","setEnabled",false);}command("tp BridgeLive "+kb.getX()+" 120 "+(kb.getZ()-1)+" 0 0");}
   if(t==235){check(!kb.onGround(),"midair removal fixture did not reach air");item(Items.DIAMOND_BLOCK);int count=g.player.getMainHandItem().getCount();click(kb);check(kb.isRemoved()&&g.level.getEntity(kb.getId())==null&&g.player.getMainHandItem().getCount()==count,"diamond block did not remove kb dummy in flight");pass("diamond-block hit removes airborne kb dummy without consuming block");}
   if(t==245){command("tp BridgeLive -255.5 120 -255.5 0 0");command("fill -258 120 -252 -253 124 -252 stone");}
   if(t==250){g.player.setYRot(0);g.player.setXRot(0);item(Items.STICK);chat("/spawnnpc kb");kb=npcs().stream().filter(TestNpc::hasKnockback).findFirst().orElseThrow();ids.add(kb.getId());}
   if(t==260){item(Items.STICK);click(kb);check(kb.getDeltaMovement().z>.35,"wall fixture hit did not apply KB");}
   if(t>=260&&t<=280)check(kb.getBoundingBox().maxZ<=-252+.00001&&g.level.noCollision(kb,kb.getBoundingBox()),"kb dummy passed through solid wall "+kb.position());
   if(t==280){check(kb.onGround()&&kb.getBoundingBox().maxZ<=-252+.00001,"wall collision/landing failed");pass("kb movement collides with solid wall instead of phasing through it");item(Items.DIAMOND_BLOCK);g.gameMode.attack(g.player,kb);command("fill -258 120 -252 -253 124 -252 air");command("tp BridgeLive -255.5 120 -255.5 0 0");}
   if(t==300){g.player.setYRot(0);g.player.setXRot(0);item(Items.STICK);chat("/spawnnpc kb");kb=npcs().stream().filter(TestNpc::hasKnockback).findFirst().orElseThrow();ids.add(kb.getId());command("tp BridgeLive "+(kb.getX()-1.5)+" 120 "+(kb.getZ()-1.5)+" -45 0");}
   if(t==315){item(Items.STICK);click(kb);var v=kb.getDeltaMovement();check(v.x>.2&&v.z>.2&&Math.abs(v.x-v.z)<.01&&v.y>.35,"diagonal hit direction ignored "+v);pass("diagonal hit knocks away from attacker position in both horizontal axes");}
   if(t==340){item(Items.DIAMOND_BLOCK);g.gameMode.attack(g.player,kb);}
   if(t==360){item(Items.STICK);g.player.setYRot(0);g.player.setXRot(0);chat("/spawnnpc");for(var p:npcs())ids.add(p.getId());oldWorld=g.level;oldNpcs.addAll(npcs());check(oldNpcs.size()>=2,"cleanup fixtures missing");command("execute in minecraft:the_nether run tp BridgeLive 0.5 100 0.5");}
   if(t>=380&&g.level!=oldWorld){finished=true;NpcManager.tick();check(npcs().isEmpty(),"NPCs leaked into new dimension");for(var p:oldNpcs)check(p.isRemoved()||oldWorld.getEntity(p.getId())!=p,"old world retained NPC");pass("real dimension transition clears all old-world dummies");check(fakeAttacks==0&&fakeInteractions==0&&spawnPackets==0,"local command/entity packets leaked");pass("packet audit: zero local-command packets, zero dummy attack/interact packets; ordinary server commands and attacks preserved");
    if(integration){aimHold=false;enabled("AimLockSettings","setEnabled",false);}Files.writeString(Path.of("/tmp/spawnnpc110-live/"+(integration?"integration":"standalone")+"-results.txt"),String.join("\n",results)+"\n");System.out.println("NPC ALL CHECKS COMPLETE");command("execute in minecraft:overworld run tp BridgeLive -255.5 120 -255.5");g.stop();}
   if(t>480)throw new AssertionError("dimension transition timed out");
  }catch(Exception e){throw new RuntimeException(e);}
 }
}
