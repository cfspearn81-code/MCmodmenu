package net.spawnnpc.mixin;

import net.minecraft.client.Minecraft;
import net.spawnnpc.NpcManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
abstract class NpcLifecycleMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void spawnnpc$tick(CallbackInfo callback) { NpcManager.tick(); }
}
