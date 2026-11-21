package net.fastersprint;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

/** Compresses a normal grounded jump's ascent into one collision-resolved movement. */
public final class QuickJump {
    private QuickJump() { }
    public static void afterJump(LivingEntity entity, float airDrag) {
        var game = Minecraft.getInstance();
        if (entity != game.player || !(entity instanceof LocalPlayer player) || game.level == null
                || !QuickJumpSettings.enabled() || !player.onGround() || !player.input.keyPresses.jump()
                || !player.isAlive() || player.isSpectator() || player.isPassenger()
                || player.getAbilities().flying || player.isFallFlying() || player.isInWater()
                || player.isInLava() || player.onClimbable() || player.hasEffect(MobEffects.LEVITATION)
                || FreeCam.active()) return;
        var velocity = player.getDeltaMovement();
        double rise = QuickJumpPhysics.rise(velocity.y, player.getGravity(), player.shouldDiscardFriction() ? 1 : airDrag);
        if (rise <= 0) return;
        double previousY = player.getY();
        player.move(MoverType.SELF, new Vec3(0, rise, 0));
        double moved = player.getY() - previousY;
        // Remove only the ascent: preserve vanilla sprint-jump horizontal momentum and gravity.
        player.setDeltaMovement(velocity.x, 0, velocity.z);
        // Display the new height immediately, without interpolating from the takeoff position.
        player.yo += moved;
        player.yOld += moved;
    }
}
