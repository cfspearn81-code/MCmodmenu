package check.mixin;
import check.MenuCheck;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Gui.class)
public abstract class FrameMixin {
 @Inject(method="extractRenderState",at=@At("HEAD"))private void check$before(CallbackInfo ci){MenuCheck.beforeFrame();}
 @Inject(method="extractRenderState",at=@At("TAIL"))private void check$after(CallbackInfo ci){MenuCheck.afterFrame();}
}
