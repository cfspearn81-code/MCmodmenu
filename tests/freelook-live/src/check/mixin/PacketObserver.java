package check.mixin;

import check.FreeLookCheck;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class PacketObserver {
    @Inject(method = "send", at = @At("HEAD"))
    private void check$observe(Packet<?> packet, CallbackInfo callback) {
        FreeLookCheck.packet(packet);
    }
}
