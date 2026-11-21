package net.fastersprint.mixin;

import net.fastersprint.ClientMenu;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.Overlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
abstract class MenuScreenLifecycleMixin {
    @Inject(method = "setScreen", at = @At("HEAD"))
    private void fastersprint$dismissForScreen(Screen screen, CallbackInfo callback) {
        if (screen != null) ClientMenu.screenOpening();
    }

    @Inject(method = "setOverlay", at = @At("HEAD"))
    private void fastersprint$dismissForOverlay(Overlay overlay, CallbackInfo callback) {
        if (overlay != null) ClientMenu.screenOpening();
    }
}
