package net.fastersprint.mixin;

import net.fastersprint.QuickJump;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
abstract class QuickJumpMixin {
    @Shadow protected abstract float getAirDrag();

    @Inject(method = "jumpFromGround", at = @At("TAIL"))
    private void fastersprint$reachJumpPeak(CallbackInfo callback) {
        QuickJump.afterJump((LivingEntity)(Object)this, getAirDrag());
    }
}
