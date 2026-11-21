package net.fastersprint.mixin;

import net.fastersprint.FreeCam;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
abstract class FreeCamHandMixin {
    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void fastersprint$hideDetachedHands(CallbackInfo callback) { if (FreeCam.active()) callback.cancel(); }
    @Inject(method = "shouldRenderBlockOutline", at = @At("HEAD"), cancellable = true)
    private void fastersprint$hideDetachedTarget(CallbackInfoReturnable<Boolean> callback) { if (FreeCam.active()) callback.setReturnValue(false); }
}
