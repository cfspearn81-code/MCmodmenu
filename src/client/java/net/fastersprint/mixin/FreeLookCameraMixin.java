package net.fastersprint.mixin;

import net.fastersprint.FreeLook;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
abstract class FreeLookCameraMixin {
    @Shadow private Level level;
    @Shadow protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "update", at = @At("HEAD"))
    private void fastersprint$syncFreeLook(DeltaTracker delta, CallbackInfo callback) {
        if (fastersprint$isMainCamera()) FreeLook.tick();
    }

    @Redirect(method = "alignWithEntity", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/Options;getCameraType()Lnet/minecraft/client/CameraType;"))
    private CameraType fastersprint$orbitPerspective(Options options) {
        return fastersprint$isMainCamera() ? FreeLook.cameraType(options) : options.getCameraType();
    }

    @Inject(method = "getMaxZoom", at = @At("HEAD"), cancellable = true)
    private void fastersprint$orbitWithoutCollision(float distance, CallbackInfoReturnable<Float> callback) {
        if (fastersprint$isMainCamera() && FreeLook.active()) {
            // Vanilla has positioned the camera at the interpolated eye anchor and is about to move it back.
            setRotation(FreeLook.yaw(), FreeLook.pitch());
            callback.setReturnValue(distance);
        }
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void fastersprint$orbitRenderState(CameraRenderState state, DeltaTracker delta, CallbackInfo callback) {
        if (fastersprint$isMainCamera() && FreeLook.active()) {
            state.isFirstPerson = false;
            // Like spectator mode, bypass terrain occlusion only while the camera is inside solid terrain.
            if (state.smartCull && level.getBlockState(state.blockPos).isSolidRender()) {
                state.smartCull = false;
            }
        }
    }

    @org.spongepowered.asm.mixin.Unique
    private boolean fastersprint$isMainCamera() {
        return (Object) this == Minecraft.getInstance().gameRenderer.mainCamera();
    }
}
