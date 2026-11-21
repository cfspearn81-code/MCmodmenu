package net.fastersprint.mixin;

import net.fastersprint.FreeCam;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MouseHandler.class)
abstract class FreeCamMouseMixin {
    @Redirect(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
    private void fastersprint$turnCamera(LocalPlayer player, double x, double y) {
        if (FreeCam.active()) FreeCam.turn(x,y); else player.turn(x,y);
    }
}
