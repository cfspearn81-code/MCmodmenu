package net.fastersprint.mixin;

import net.fastersprint.FreeCam;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
abstract class FreeCamInteractionMixin {
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void fastersprint$noDetachedAttack(CallbackInfoReturnable<Boolean> callback) {
        if (FreeCam.active()) callback.setReturnValue(false);
    }
    @Inject(method = {"continueAttack", "startUseItem"}, at = @At("HEAD"), cancellable = true)
    private void fastersprint$noDetachedUse(CallbackInfo callback) {
        if (FreeCam.active()) callback.cancel();
    }
}
