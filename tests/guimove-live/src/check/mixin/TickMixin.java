package check.mixin;
import check.GuiCheck;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(Minecraft.class)
public class TickMixin {
 @Inject(method="tick",at=@At("HEAD"))private void check$tick(CallbackInfo ci){GuiCheck.tick();}
 @Inject(method="isWindowActive",at=@At("RETURN"),cancellable=true)private void check$focus(CallbackInfoReturnable<Boolean> ci){ci.setReturnValue(GuiCheck.focused);}
}
