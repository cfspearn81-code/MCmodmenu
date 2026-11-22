package net.fastersprint;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Acquire ahead once, then preserve the same target until death or hold-key release. */
public final class AimLock {
    private static final double ACQUIRE_RANGE_SQUARED = 32 * 32;
    private static final double ACQUIRE_DOT = Math.cos(Math.toRadians(45));
    private static LocalPlayer actor;
    private static ClientLevel world;
    private static Player target;
    private static int selectedTick=Integer.MIN_VALUE, boundKey=-1;
    private AimLock() { }
    private static void clear(){actor=null;world=null;target=null;selectedTick=Integer.MIN_VALUE;boundKey=-1;}
    private static boolean held(){return InputConstants.isKeyDown(AimLockSettings.key());}
    private static boolean candidate(Player player,LocalPlayer self,ClientLevel level){
        return player!=self&&player.level()==level&&player.isAlive()&&!player.isRemoved()
            &&!player.isSpectator()&&player.isAttackable()&&!player.isInvisibleTo(self);
    }
    private static boolean visible(ClientLevel level,LocalPlayer self,Vec3 from,Vec3 head){
        return level.clip(new ClipContext(from,head,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,self)).getType()!=HitResult.Type.BLOCK;
    }
    public static void update(float partialTick){
        Minecraft game=Minecraft.getInstance();LocalPlayer self=game.player;ClientLevel level=game.level;
        if(!AimLockSettings.enabled()||self==null||level==null||!self.isAlive()||self.isSpectator()||!held()){clear();return;}
        int key=AimLockSettings.key();
        if(actor!=self||world!=level||boundKey!=key){clear();actor=self;world=level;boundKey=key;}
        // Pause view changes without forgetting an existing lock. Key release still clears it above.
        if(game.getCameraEntity()!=self||FreeCam.active()||FreeLook.active()||game.isPaused()||!game.isWindowActive()
                ||game.gui.screen()!=null||game.gui.overlay()!=null||ClientMenu.isOpen())return;
        if(target!=null&&target.isDeadOrDying()){
            target=null;selectedTick=Integer.MIN_VALUE;
        }
        // Servers can temporarily replace/remove an entity without killing the player. Preserve UUID identity.
        if(target!=null&&(target.isRemoved()||target.level()!=level)){
            if(selectedTick!=self.tickCount){
                selectedTick=self.tickCount;
                for(Player player:level.players())if(player.getUUID().equals(target.getUUID())){target=player;break;}
            }
            if(target.isRemoved()||target.level()!=level)return;
        }
        Vec3 eye=self.getEyePosition(partialTick);
        if(target==null&&selectedTick!=self.tickCount){
            selectedTick=self.tickCount;double closest=ACQUIRE_RANGE_SQUARED;
            Vec3 forward=self.getViewVector(1).normalize();
            for(Player player:level.players()){
                if(!candidate(player,self,level))continue;
                Vec3 head=player.getEyePosition(partialTick),direction=head.subtract(eye);
                double distance=direction.lengthSqr();
                if(distance<1e-12||distance>closest||forward.dot(direction)/Math.sqrt(distance)<ACQUIRE_DOT
                        ||!visible(level,self,eye,head))continue;
                closest=distance;target=player;
            }
        }
        // Once locked, follow the same head through walls. Invisibility and spectator state still pause aiming.
        if(target==null||!candidate(target,self,level))return;
        Vec3 direction=target.getEyePosition(partialTick).subtract(eye);
        var look=AimAngles.toward(direction.x,direction.y,direction.z,self.getYRot());
        self.setYRot(look.yaw());self.setXRot(look.pitch());self.yRotO=look.yaw();self.xRotO=look.pitch();
        self.setYHeadRot(look.yaw());self.yHeadRotO=look.yaw();
        // Ordinary player rotation: vanilla sends its usual movement/look packets and picks the real crosshair.
    }
}
