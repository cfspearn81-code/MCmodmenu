package preview.mixin;
import preview.VisualCheck;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(Minecraft.class)
public abstract class PreviewMixin {
 @Inject(method="tick",at=@At("HEAD"))private void preview$tick(CallbackInfo ci){VisualCheck.tick();}
 @Inject(method="isWindowActive",at=@At("RETURN"),cancellable=true)private void preview$focus(CallbackInfoReturnable<Boolean> ci){ci.setReturnValue(VisualCheck.focused);}
}
