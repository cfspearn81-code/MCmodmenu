package net.fastersprint.mixin;

import net.fastersprint.TintSettings;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
abstract class SkyTintFogMixin {
    @Inject(method="setupFog",at=@At("RETURN"))
    private void fastersprint$keepSkyPalette(Camera camera,int chunks,DeltaTracker delta,float darkening,ClientLevel level,CallbackInfoReturnable<FogData> ci){
        if(TintSettings.SKY.enabled()&&level.dimensionType().skybox()==DimensionType.Skybox.OVERWORLD&&camera.getFluidInCamera()==FogType.NONE){
            var data=ci.getReturnValue();var color=TintSettings.SKY.vector();
            data.color.set(color.x(),color.y(),color.z(),data.color.w());
        }
        // The clear background and distant haze must match the sky disc, including below its horizon.
        // Keep every native fog distance and preserve underwater/lava/dimension-specific colors.
    }
}
