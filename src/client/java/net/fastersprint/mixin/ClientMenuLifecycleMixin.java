package net.fastersprint.mixin;

import net.fastersprint.ClientMenu;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
abstract class ClientMenuLifecycleMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void fastersprint$updateMenu(CallbackInfo callback) {
        ClientMenu.tick();
        net.fastersprint.FreeCam.tick();
        net.fastersprint.FreeLook.tick();
        net.fastersprint.AimLock.update(1);
        net.fastersprint.WallClimb.tick();
        net.fastersprint.QuickFall.lifecycle();
    }
    @Inject(method = "tick", at = @At("TAIL"))
    private void fastersprint$autoHit(CallbackInfo callback) {
        net.fastersprint.AutoHit.tick();
        net.fastersprint.Xray.tick();
        net.fastersprint.BedEsp.tick();
    }

}
