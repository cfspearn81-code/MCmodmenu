package net.fastersprint.mixin;

import net.fastersprint.FreeCam;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
abstract class FreeCamCameraMixin {
    @Shadow private boolean detached;
    @Shadow protected abstract void setPosition(Vec3 position);
    @Shadow protected abstract void setRotation(float yaw, float pitch);
    // Before vanilla builds its cull frustum/projection, so terrain and ESP use the same camera.
    @Inject(method = "alignWithEntity", at = @At("RETURN"))
    private void fastersprint$detachCamera(float partialTick, CallbackInfo callback) {
        if (!FreeCam.active()) return;
        detached = true;
        setPosition(FreeCam.position(partialTick));
        setRotation(FreeCam.yaw(), FreeCam.pitch());
    }
    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void fastersprint$detachedView(CameraRenderState state, DeltaTracker delta, CallbackInfo callback) {
        if (FreeCam.active()) { state.isFirstPerson = false; state.smartCull = false; }
    }
}
