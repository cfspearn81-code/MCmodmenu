package check.mixin;
import check.MenuCheck;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(GuiRenderState.class)
public abstract class BlurMixin {
 @Inject(method="blurBeforeThisStratum",at=@At("HEAD"))private void check$count(CallbackInfo ci){MenuCheck.blurs++;}
}
