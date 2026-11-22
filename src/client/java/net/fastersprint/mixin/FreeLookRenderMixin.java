package net.fastersprint.mixin;

import net.fastersprint.FreeLook;
import net.minecraft.client.CameraType;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
abstract class FreeLookRenderMixin {
    @Redirect(method = "extractOptions", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/Options;getCameraType()Lnet/minecraft/client/CameraType;"))
    private CameraType fastersprint$orbitRenderPerspective(Options options) {
        return FreeLook.cameraType(options);
    }
}
