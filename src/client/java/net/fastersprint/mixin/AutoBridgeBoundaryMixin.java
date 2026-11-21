package net.fastersprint.mixin;

import net.fastersprint.AutoBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
abstract class AutoBridgeBoundaryMixin {
    @Inject(method = "maybeBackOffFromEdge", at = @At("RETURN"), cancellable = true)
    private void fastersprint$limitBridgeStep(Vec3 movement, MoverType type,
            CallbackInfoReturnable<Vec3> callback) {
        if ((Object) this == Minecraft.getInstance().player && type == MoverType.SELF) {
            callback.setReturnValue(AutoBridge.guardMovement(callback.getReturnValue()));
        }
    }
}
