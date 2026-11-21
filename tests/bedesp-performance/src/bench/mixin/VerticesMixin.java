package bench.mixin;
import bench.Profile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(targets="net.fastersprint.BedOutlineState",remap=false)
public abstract class VerticesMixin {
 private long begin;
 @Inject(method="buildVertices",at=@At("HEAD"))private void begin(CallbackInfo ci){begin=System.nanoTime();}
 @Inject(method="buildVertices",at=@At("RETURN"))private void end(CallbackInfo ci){Profile.vertexTime(System.nanoTime()-begin);}
}
