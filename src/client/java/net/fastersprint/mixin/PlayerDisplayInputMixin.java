package net.fastersprint.mixin;

import net.fastersprint.ClientMenu;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
abstract class PlayerDisplayInputMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void fastersprint$handleMenuKeys(
            long window, int action, KeyEvent event, CallbackInfo callback) {
        Minecraft game = Minecraft.getInstance();
        if (window == game.getWindow().handle() && game.isWindowActive()
                && ClientMenu.handleKey(action, event)) {
            callback.cancel();
        }
    }
}
