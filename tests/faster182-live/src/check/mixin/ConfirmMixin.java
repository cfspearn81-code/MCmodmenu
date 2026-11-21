package check.mixin;
import check.GhostCheck;
import net.fastersprint.AutoBridge;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=AutoBridge.class,remap=false)
public abstract class ConfirmMixin {
 @Inject(method="acknowledge",at=@At("RETURN"))private static void confirmed(ClientLevel level,int sequence,CallbackInfo ci){GhostCheck.confirmation();}
}
