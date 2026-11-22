package check.mixin;
import check.AimCheck;
import net.fastersprint.AimLock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=AimLock.class,remap=false)
public abstract class HoldMixin {
 @Redirect(method="held",at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/platform/InputConstants;isKeyDown(I)Z"))private static boolean held(int key){AimCheck.lastReadKey=key;return AimCheck.hold;}
}
