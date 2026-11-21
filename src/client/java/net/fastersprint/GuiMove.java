package net.fastersprint;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.sdl.SDLMouse;

/** Physical movement input remains independent of the screen's normal text/button handling. */
public final class GuiMove {
    private GuiMove() { }
    public static boolean active(Minecraft game) {
        return PlayerDisplaySettings.guiMoveEnabled() && game.player != null && game.level != null
            && game.player.isAlive() && game.gui.screen() != null && game.gui.overlay() == null
            && game.isWindowActive();
    }
    public static boolean readKey(KeyMapping binding) {
        Minecraft game = Minecraft.getInstance();
        if (!PlayerDisplaySettings.guiMoveEnabled() || game.gui.screen() == null) return binding.isDown();
        if (!active(game) || binding.isUnbound()) return false;
        InputConstants.Key key = InputConstants.getKey(binding.saveString());
        if (key.getType() == InputConstants.Type.KEYBOARD) return keyDown(key.getValue());
        if (key.getType() == InputConstants.Type.MOUSE) {
            int mask = 1 << (key.getValue() - 1);
            return (SDLMouse.SDL_GetMouseState(null, null) & mask) != 0;
        }
        return false;
    }
    private static boolean keyDown(int key) { return InputConstants.isKeyDown(key); }
}
