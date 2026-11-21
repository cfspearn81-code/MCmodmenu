package net.fastersprint.mixin;

import net.fastersprint.AutoBridge;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
abstract class AutoBridgeAcknowledgementMixin {
    @Inject(method = "handleBlockChangedAck", at = @At("TAIL"))
    private void fastersprint$confirmPlacement(int sequence, CallbackInfo callback) {
        AutoBridge.acknowledge((ClientLevel) (Object) this, sequence);
    }
}
