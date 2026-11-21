package check.mixin;
import check.XrayCheck;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(Minecraft.class)
public class CheckMixin {
 @Inject(method="tick",at=@At("HEAD"))private void check$tick(CallbackInfo callback){XrayCheck.tick();}
 @Inject(method="isWindowActive",at=@At("RETURN"),cancellable=true)private void check$focus(CallbackInfoReturnable<Boolean> callback){callback.setReturnValue(true);}
}
