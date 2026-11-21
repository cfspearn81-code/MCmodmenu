package net.fastersprint;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.sdl.SDLMouse;

final class MenuCursor {
    private static final CursorTrail TRAIL = new CursorTrail();
    private static boolean managed;
    private static boolean wasVisible;
    private MenuCursor() { }
    static void begin() {
        TRAIL.clear();
        if (!managed) { wasVisible = SDLMouse.SDL_CursorVisible(); managed = true; }
        SDLMouse.SDL_HideCursor();
    }
    static void end() {
        if (managed) {
            if (wasVisible) SDLMouse.SDL_ShowCursor(); else SDLMouse.SDL_HideCursor();
        }
        managed = false; TRAIL.clear();
    }
    static void tick(boolean focused) {
        if (!managed) return;
        if (focused) {
            if (SDLMouse.SDL_CursorVisible()) SDLMouse.SDL_HideCursor();
        } else {
            if (wasVisible) SDLMouse.SDL_ShowCursor();
            TRAIL.clear();
        }
    }
    static void render(GuiGraphicsExtractor g) {
        Minecraft game = Minecraft.getInstance();
        if (!managed || !game.isWindowActive()) return;
        float x = (float) game.mouseHandler.getScaledXPos(game.getWindow());
        float y = (float) game.mouseHandler.getScaledYPos(game.getWindow());
        long now = System.nanoTime();
        TRAIL.update(x, y, now);
        g.nextStratum();
        for (var sample : TRAIL.snapshot(now)) {
            float life = sample.remaining(now);
            int alpha = Math.round(140 * life * life);
            MenuPaint.circle(g, sample.x(), sample.y(), 0.6F + 1.4F * life, alpha << 24 | 0xFFFFFF);
        }
        MenuPaint.circle(g, x, y + 0.5F, 3.3F, 0x38000000);
        MenuPaint.circle(g, x, y, 3.0F, 0x40FFFFFF);
        MenuPaint.circle(g, x, y, 2.1F, 0xFFFFFFFF);
    }
}
