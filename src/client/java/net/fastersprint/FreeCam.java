package net.fastersprint;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

/** A detached render camera. Player physics/keepalives and server corrections remain vanilla. */
public final class FreeCam {
    private static boolean enabled;
    private static LocalPlayer actor;
    private static ClientLevel world;
    private static Vec3 previous = Vec3.ZERO, position = Vec3.ZERO;
    private static float yaw, pitch;
    private FreeCam() { }
    public static boolean active() {
        Minecraft game = Minecraft.getInstance();
        return enabled && actor == game.player && world == game.level && actor != null && actor.isAlive();
    }
    public static void toggle() { if (active()) disable(); else enable(); }
    public static void enable() {
        Minecraft game = Minecraft.getInstance();
        if (game.player == null || game.level == null || !game.player.isAlive()) return;
        actor = game.player; world = game.level;
        position = actor.getEyePosition(); previous = position;
        yaw = actor.getYRot(); pitch = actor.getXRot(); enabled = true;
        discardActions(game);
    }
    public static void disable() { enabled = false; actor = null; world = null; }
    public static boolean handleKey(int action, KeyEvent event) {
        Minecraft game = Minecraft.getInstance();
        if (game.player == null || game.gui.screen() != null || game.gui.overlay() != null || ClientMenu.isOpen()) return false;
        if (event.key() != FreeCamSettings.key()) return false;
        if (action == InputConstants.PRESS) toggle();
        return true; // Repeat/release never toggle, and this binding is not also a player action.
    }
    public static void tick() {
        Minecraft game = Minecraft.getInstance();
        if (enabled && !active()) { disable(); return; }
        if (!active()) return;
        previous = position;
        discardActions(game);
        if (!game.isWindowActive() || game.isPaused() || ClientMenu.isOpen()
                || game.gui.screen() != null || game.gui.overlay() != null) return;
        int forward = (game.options.keyUp.isDown()?1:0)-(game.options.keyDown.isDown()?1:0);
        int right = (game.options.keyRight.isDown()?1:0)-(game.options.keyLeft.isDown()?1:0);
        int vertical = (game.options.keyJump.isDown()?1:0)-(game.options.keyShift.isDown()?1:0);
        position = position.add(FreeCamMotion.step(yaw, pitch, forward, right, vertical, game.options.keySprint.isDown()));
    }
    private static void discardActions(Minecraft game) {
        for (var key : new net.minecraft.client.KeyMapping[]{game.options.keyAttack, game.options.keyUse, game.options.keyDrop, game.options.keySwapOffhand}) {
            key.setDown(false); while (key.consumeClick()) { }
        }
    }
    public static void turn(double horizontal, double vertical) {
        yaw = (float) ((yaw + horizontal * .15) % 360);
        pitch = (float) Math.clamp(pitch + vertical * .15, -90, 90);
    }
    public static Vec3 position(float partialTick) { return previous.lerp(position, Math.clamp(partialTick,0F,1F)); }
    public static float yaw() { return yaw; }
    public static float pitch() { return pitch; }
    public static void renderStatus(GuiGraphicsExtractor graphics) {
        if (!active() || ClientMenu.isOpen()) return;
        var game = Minecraft.getInstance();
        if (game.gui.hud.isHidden() || game.gui.screen() != null) return;
        String key = InputConstants.Type.KEYBOARD.getOrCreate(FreeCamSettings.key()).getDisplayName().getString();
        graphics.centeredText(game.font, "FREE CAM  |  " + key + " to return", graphics.guiWidth()/2, 12, 0xFFC6B1FF);
    }
}
