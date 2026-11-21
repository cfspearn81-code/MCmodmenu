package check.mixin;
import check.GuiCheck;
import net.fastersprint.GuiMove;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=GuiMove.class,remap=false)
public class KeysMixin {
 @Redirect(method="keyDown",at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/platform/InputConstants;isKeyDown(I)Z"))
 private static boolean check$key(int key){return GuiCheck.key(key);}
}
