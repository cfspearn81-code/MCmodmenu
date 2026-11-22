package net.fastersprint.mixin;

import net.fastersprint.AutoBridge;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
abstract class LocalPlayerMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void fastersprint$tickAutoBridge(CallbackInfo callback) {
        AutoBridge.tick();
    }
}
