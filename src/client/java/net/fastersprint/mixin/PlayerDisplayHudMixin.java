package net.fastersprint.mixin;

import net.fastersprint.PlayerDisplayRenderer;
import net.fastersprint.ClientMenu;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
abstract class PlayerDisplayHudMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void fastersprint$drawPlayerDirections(
            GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo callback) {
        PlayerDisplayRenderer.render(graphics, delta);
        ClientMenu.render(graphics);
    }
}
