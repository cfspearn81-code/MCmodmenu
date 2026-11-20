package net.fastersprint.mixin;

import net.fastersprint.PlayerDisplayRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AvatarRenderer.class)
abstract class PlayerDisplayNameMixin {
    @Inject(
        method = "shouldShowName(Lnet/minecraft/world/entity/Avatar;D)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void fastersprint$usePlayerDisplayName(
            Avatar entity, double distanceSquared, CallbackInfoReturnable<Boolean> callback) {
        if (PlayerDisplayRenderer.shouldHighlight(entity)) {
            callback.setReturnValue(false);
        }
    }
}
