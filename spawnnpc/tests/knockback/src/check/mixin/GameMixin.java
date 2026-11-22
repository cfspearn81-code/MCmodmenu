package check.mixin;
import check.NpcCheck;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(Minecraft.class)
public abstract class GameMixin {
 @Inject(method="tick",at=@At("HEAD"))private void tick(CallbackInfo ci){NpcCheck.tick();}
 @Inject(method="isWindowActive",at=@At("RETURN"),cancellable=true)private void focus(CallbackInfoReturnable<Boolean> ci){ci.setReturnValue(true);}
}
