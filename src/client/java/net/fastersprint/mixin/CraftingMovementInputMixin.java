package net.fastersprint.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.fastersprint.PlayerDisplaySettings;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.player.KeyboardInput;
import org.lwjgl.sdl.SDLMouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(KeyboardInput.class)
abstract class CraftingMovementInputMixin {
    @Redirect(method = "tick", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/KeyMapping;isDown()Z"))
    private boolean fastersprint$readCraftingMovementKey(KeyMapping binding) {
        Minecraft game = Minecraft.getInstance();
        if (!(game.gui.screen() instanceof CraftingScreen crafting)
                || !PlayerDisplaySettings.craftingMovementEnabled()) {
            return binding.isDown();
        }
        if (!game.isWindowActive() || game.gui.overlay() != null
                || crafting.isInputCaptured() || binding.isUnbound()) {
            return false;
        }

        InputConstants.Key key = InputConstants.getKey(binding.saveString());
        if (key.getType() == InputConstants.Type.KEYBOARD) {
            return InputConstants.isKeyDown(key.getValue());
        }
        if (key.getType() == InputConstants.Type.MOUSE) {
            int buttonMask = 1 << (key.getValue() - 1);
            return (SDLMouse.SDL_GetMouseState(null, null) & buttonMask) != 0;
        }
        return false;
    }
}
