package bench.mixin;
import bench.Profile;
import net.minecraft.client.renderer.state.gui.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(GuiRenderState.class)
public abstract class StatesMixin {
 @Inject(method="addGuiElement",at=@At("HEAD"))private void count(GuiElementRenderState state,CallbackInfo ci){if(Profile.active)Profile.fills++;}
}
