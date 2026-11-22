package check.mixin;
import check.NpcCheck;
import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(InputConstants.class)
public abstract class HoldMixin {
 @Inject(method="isKeyDown",at=@At("HEAD"),cancellable=true)private static void hold(int key,CallbackInfoReturnable<Boolean> ci){if(NpcCheck.integration&&key==InputConstants.KEY_R)ci.setReturnValue(NpcCheck.aimHold);}
}
