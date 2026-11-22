package check.mixin;
import check.AimCheck;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class PacketMixin {
 @Inject(method="send",at=@At("HEAD"))private void sent(Packet<?> packet,CallbackInfo ci){AimCheck.packet(packet);}
}
