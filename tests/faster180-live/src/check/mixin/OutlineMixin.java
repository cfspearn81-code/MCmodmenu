package check.mixin;
import check.TintCheck;
import net.fastersprint.PlayerTint;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=PlayerTint.class,remap=false)
public abstract class OutlineMixin {
 @Inject(method="emit",at=@At("HEAD"))private static void observe(float[] positions,int color,VertexConsumer out,CallbackInfo ci){TintCheck.outlines++;TintCheck.vertices=positions.length/3;TintCheck.outlineColor=color;for(float f:positions)if(!Float.isFinite(f))throw new AssertionError("non-finite model edge");}
}
