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
        if(TintSettings.SKY.enabled()&&level.dimensionType().skybox()==DimensionType.Skybox.OVERWORLD&&camera.getFluidInCamera()==FogType.NONE)
            ci.getReturnValue().skyEnd=1_000_000_000F;
        // Only the sky's blend distance changes. Terrain, cloud and environmental fog stay native.
    }
}
