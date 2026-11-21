package bench.mixin;
import bench.Profile;
import net.fastersprint.BedEsp;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(value=BedEsp.class,remap=false)
public abstract class ProfileMixin {
 private static long tickStart;
 @Inject(method="render",at=@At("HEAD"))private static void begin(GuiGraphicsExtractor g,CallbackInfo ci){Profile.begin();}
 @Inject(method="render",at=@At("RETURN"))private static void end(GuiGraphicsExtractor g,CallbackInfo ci){Profile.end();}
 @Inject(method="tick",at=@At("HEAD"))private static void tickBegin(CallbackInfo ci){tickStart=System.nanoTime();}
 @Inject(method="tick",at=@At("RETURN"))private static void tickEnd(CallbackInfo ci){Profile.tickTime(System.nanoTime()-tickStart);}
}
