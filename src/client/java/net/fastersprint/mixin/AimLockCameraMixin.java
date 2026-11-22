package net.fastersprint.mixin;

import net.fastersprint.AimLock;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
abstract class AimLockCameraMixin {
    @Inject(method="alignWithEntity",at=@At("HEAD"))
    private void fastersprint$trackHead(float partialTick,CallbackInfo callback){
        if((Object)this==Minecraft.getInstance().gameRenderer.mainCamera())AimLock.update(partialTick);
    }
}
