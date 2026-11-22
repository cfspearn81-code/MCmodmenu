package net.fastersprint.mixin;

import net.fastersprint.PlayerDisplayRenderer;
import net.fastersprint.ClientMenu;
import net.fastersprint.FreeLook;
import net.minecraft.client.CameraType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
abstract class PlayerDisplayHudMixin {
    @Redirect(method = {"extractCameraOverlays", "extractCrosshair"}, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/Options;getCameraType()Lnet/minecraft/client/CameraType;"))
    private CameraType fastersprint$orbitHudPerspective(Options options) {
        return FreeLook.cameraType(options);
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void fastersprint$drawPlayerDirections(
            GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo callback) {
        net.fastersprint.Xray.render(graphics);
        net.fastersprint.BedEsp.render(graphics);
        PlayerDisplayRenderer.render(graphics, delta);
        net.fastersprint.FreeCam.renderStatus(graphics);
        ClientMenu.render(graphics);
    }
}
