package check.mixin;
import check.GhostCheck;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ClientLevel.class)
public abstract class AckMixin {
 @Inject(method="handleBlockChangedAck",at=@At("HEAD"),cancellable=true)private void delay(int sequence,CallbackInfo ci){if(GhostCheck.delay((ClientLevel)(Object)this,sequence))ci.cancel();}
}
