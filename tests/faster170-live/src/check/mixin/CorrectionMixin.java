package check.mixin;
import check.JumpCheck;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ClientPacketListener.class)
public abstract class CorrectionMixin {
 @Inject(method="handleMovePlayer",at=@At("RETURN"))private void check$correction(ClientboundPlayerPositionPacket packet,CallbackInfo ci){JumpCheck.correction(packet);}
}
