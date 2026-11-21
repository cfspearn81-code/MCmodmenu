package live.mixin;
import live.LiveTest;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(Minecraft.class)
public abstract class TickMixin {
 @Inject(method="tick",at=@At("HEAD")) private void live$tick(CallbackInfo ci){LiveTest.tick();}
 @Inject(method="isWindowActive",at=@At("RETURN"),cancellable=true) private void live$focus(CallbackInfoReturnable<Boolean> ci){ci.setReturnValue(true);}
}
