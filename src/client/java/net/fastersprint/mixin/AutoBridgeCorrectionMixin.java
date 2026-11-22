package net.fastersprint.mixin;

import net.fastersprint.AutoBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
abstract class AutoBridgeCorrectionMixin {
    @Inject(method = "handleMovePlayer", at = @At("TAIL"))
    private void fastersprint$pauseAfterCorrection(ClientboundPlayerPositionPacket packet, CallbackInfo callback) {
        AutoBridge.serverMovement();
    }

    @Inject(method = "handleSetEntityMotion", at = @At("TAIL"))
    private void fastersprint$pauseAfterPush(ClientboundSetEntityMotionPacket packet, CallbackInfo callback) {
        var player = Minecraft.getInstance().player;
        if (player != null && player.getId() == packet.id() && packet.movement().horizontalDistanceSqr() > 0) {
            AutoBridge.serverMovement();
        }
    }
}
