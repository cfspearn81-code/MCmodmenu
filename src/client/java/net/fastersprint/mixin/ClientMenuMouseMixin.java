package net.fastersprint.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.fastersprint.ClientMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
abstract class ClientMenuMouseMixin {
    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void fastersprint$handleMenuClick(
            long window, MouseButtonInfo button, int action, CallbackInfo callback) {
        Minecraft game = Minecraft.getInstance();
        if (!ClientMenu.isOpen() || window != game.getWindow().handle()) {
            return;
        }
        callback.cancel();
        if (button.button() == 1 && action == InputConstants.PRESS) {
            ClientMenu.click(game.mouseHandler.getScaledXPos(game.getWindow()),
                game.mouseHandler.getScaledYPos(game.getWindow()));
        }
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void fastersprint$blockMenuHotbarScroll(
            long window, double horizontal, double vertical, CallbackInfo callback) {
        if (ClientMenu.isOpen()) {
            callback.cancel();
        }
    }
}
