package check.mixin;
import check.AimCheck;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Camera.class)
public abstract class CameraMixin {
 @Inject(method="alignWithEntity",at=@At("RETURN"))private void frame(float partial,CallbackInfo ci){AimCheck.frame((Camera)(Object)this,partial);}
}
