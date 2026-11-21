package net.fastersprint.mixin;

import net.fastersprint.WallClimb;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class WallClimbMixin {
    @Inject(method = "onClimbable", at = @At("RETURN"), cancellable = true)
    private void fastersprint$useLadderPhysicsAtWall(CallbackInfoReturnable<Boolean> callback) {
        if (!callback.getReturnValue() && WallClimb.onWall((LivingEntity)(Object)this)) callback.setReturnValue(true);
        // False leaves real ladders/vines and all their existing behavior untouched.
    }
}
