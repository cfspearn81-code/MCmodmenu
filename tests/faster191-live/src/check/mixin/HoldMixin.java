package check.mixin;
import check.GhostCheck;
import net.fastersprint.AutoBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=AutoBridge.class,remap=false)
public abstract class HoldMixin {
 @Redirect(method="active",at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/platform/InputConstants;isKeyDown(I)Z"))private static boolean hold(int key){return GhostCheck.hold;}
}
