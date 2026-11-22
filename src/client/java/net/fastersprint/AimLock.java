package net.fastersprint;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Select once per player tick; track the chosen visible head smoothly at camera frame time. */
public final class AimLock {
    private static LocalPlayer actor;
    private static ClientLevel world;
    private static Player target;
    private static int selectedTick=Integer.MIN_VALUE;
    private AimLock() { }
    private static void clear(){actor=null;world=null;target=null;selectedTick=Integer.MIN_VALUE;}
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
        if(!AimLockSettings.enabled()||self==null||level==null||!self.isAlive()||self.isSpectator()
                ||game.getCameraEntity()!=self||FreeCam.active()||game.isPaused()||!game.isWindowActive()
                ||game.gui.screen()!=null||game.gui.overlay()!=null||ClientMenu.isOpen()||!held()){clear();return;}
        if(actor!=self||world!=level){clear();actor=self;world=level;}
        Vec3 eye=self.getEyePosition(partialTick);
        if(selectedTick!=self.tickCount||(target!=null&&(!candidate(target,self,level)||!visible(level,self,eye,target.getEyePosition(partialTick))))){
            target=null;selectedTick=self.tickCount;double closest=Double.POSITIVE_INFINITY;
            for(Player player:level.players()){
                if(!candidate(player,self,level))continue;
                Vec3 head=player.getEyePosition(partialTick);double distance=eye.distanceToSqr(head);
                if(distance<1e-12||distance>=closest||!visible(level,self,eye,head))continue;
                closest=distance;target=player;
            }
        }
        if(target==null)return;
        Vec3 direction=target.getEyePosition(partialTick).subtract(eye);
        var look=AimAngles.toward(direction.x,direction.y,direction.z,self.getYRot());
        self.setYRot(look.yaw());self.setXRot(look.pitch());self.yRotO=look.yaw();self.xRotO=look.pitch();
        self.setYHeadRot(look.yaw());self.yHeadRotO=look.yaw();
        // Ordinary player rotation: vanilla sends its usual movement/look packets and picks the real crosshair.
    }
}
