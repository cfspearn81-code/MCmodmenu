package net.spawnnpc.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.spawnnpc.NpcManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
abstract class NpcCommandMixin {
    @Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
    private void spawnnpc$command(String command, CallbackInfo callback) {
        if (NpcManager.command(command)) callback.cancel();
    }
}
