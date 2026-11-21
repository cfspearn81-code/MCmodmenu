package net.fastersprint.mixin;
import net.fastersprint.TintSettings;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.level.dimension.DimensionType;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(SkyRenderer.class)
abstract class SkyTintMixin {
    @Inject(method="extractRenderState",at=@At("TAIL"))
    private void fastersprint$skyColor(ClientLevel level,float partial,Camera camera,SkyRenderState state,CallbackInfo ci){
        if(TintSettings.SKY.enabled()&&state.skybox==DimensionType.Skybox.OVERWORLD){
            var color=TintSettings.SKY.vector();state.skyColor=color;
            if(state.sunriseAndSunsetColor!=null)state.sunriseAndSunsetColor=new Vector4f(color,state.sunriseAndSunsetColor.w());
        }
    }
}
