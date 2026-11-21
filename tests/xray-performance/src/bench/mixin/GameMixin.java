package bench.mixin;
import bench.Profile;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(Minecraft.class)
public abstract class GameMixin {
 @Inject(method="tick",at=@At("HEAD"))private void setup(CallbackInfo ci){Profile.setup();}
 @Inject(method="isWindowActive",at=@At("RETURN"),cancellable=true)private void focus(CallbackInfoReturnable<Boolean> ci){ci.setReturnValue(true);}
}
