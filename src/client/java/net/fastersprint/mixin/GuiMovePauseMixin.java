package net.fastersprint.mixin;

import net.fastersprint.GuiMove;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Gui.class)
abstract class GuiMovePauseMixin {
    @Inject(method = "isPausing", at = @At("RETURN"), cancellable = true)
    private void fastersprint$keepGuiMovementRunning(CallbackInfoReturnable<Boolean> callback) {
        if (GuiMove.active(Minecraft.getInstance())) callback.setReturnValue(false);
    }
}
