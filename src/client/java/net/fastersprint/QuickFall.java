package net.fastersprint;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;

/** Fast collision-resolved falls. Does not teleport, spoof grounding, or cancel fall damage. */
public final class QuickFall {
    public static final double FALL_SPEED=8;
    private static LocalPlayer actor;
    private static ClientLevel world;
    private static final QuickFallState state=new QuickFallState();
    private QuickFall() { }
    public static void reset(){actor=null;world=null;state.clear();}
    public static void lifecycle(){
        if(actor==null)return;var game=Minecraft.getInstance();
        if(actor!=game.player||world!=game.level||!actor.isAlive()||!QuickFallSettings.enabled()||FreeCam.active())reset();
    }
    public static void afterMovement(LocalPlayer player){
        var game=Minecraft.getInstance();
        if(player!=game.player||game.level==null||!QuickFallSettings.enabled()||!player.isAlive()
                ||player.isSpectator()||player.isPassenger()||player.getAbilities().flying
                ||player.isFallFlying()||player.isInWater()||player.isInLava()||FreeCam.active()){reset();return;}
        if(actor!=player||world!=game.level){reset();actor=player;world=game.level;}
        var keys=player.input.keyPresses;var move=player.input.getMoveVector();var velocity=player.getDeltaMovement();
        boolean accelerate=state.update(player.onGround(),move.lengthSquared()>0.000001F,keys.jump(),keys.shift(),player.onClimbable(),velocity.y);
        if(accelerate)player.setDeltaMovement(velocity.x,Math.min(velocity.y,-FALL_SPEED),velocity.z);
    }
}
