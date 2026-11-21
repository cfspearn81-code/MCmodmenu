package net.fastersprint.mixin;

import net.fastersprint.GuiMove;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(KeyboardInput.class)
abstract class GuiMoveInputMixin {
    @Redirect(method = "tick", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/KeyMapping;isDown()Z"))
    private boolean fastersprint$readGuiMovementKey(KeyMapping binding) {
        return GuiMove.readKey(binding);
    }
}
