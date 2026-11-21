package check.mixin;
import check.TintCheck;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(SkyRenderer.class)
public abstract class SkyMixin {
 @Inject(method="render",at=@At("HEAD"))private void observe(com.mojang.renderpearl.api.buffers.GpuBufferSlice buffer,SkyRenderState state,CallbackInfo ci){TintCheck.sky=state.skyColor;}
}
