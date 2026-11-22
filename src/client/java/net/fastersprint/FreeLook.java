package net.fastersprint;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

/** While held, route mouse motion to an orbit camera around the local player. */
public final class FreeLook {
    private static LocalPlayer player;
    private static ClientLevel level;
    private static int holdKey;
    private static float yaw, pitch;

    private FreeLook() { }

    public static void tick() {
        Minecraft game = Minecraft.getInstance();
        LocalPlayer currentPlayer = game.player;
        int key = FreeLookSettings.key();
        if (!FreeLookSettings.enabled() || currentPlayer == null || game.level == null
                || game.getCameraEntity() != currentPlayer || !currentPlayer.isAlive()
                || currentPlayer.isSpectator() || currentPlayer.isSleeping() || FreeCam.active()
                || game.isPaused() || !game.isWindowActive() || ClientMenu.isOpen()
                || game.gui.screen() != null || game.gui.overlay() != null || !InputConstants.isKeyDown(key)) {
            player = null;
            level = null;
            return;
        }
        if (player != currentPlayer || level != game.level || holdKey != key) {
            player = currentPlayer;
            level = game.level;
            holdKey = key;
            yaw = Mth.wrapDegrees(player.getYRot());
            pitch = player.getXRot();
        }
    }

    public static boolean active() { return player != null && !FreeCam.active(); }

    public static CameraType cameraType(Options options) {
        // Override rendering only, so all three F5 perspectives restore without writing the user's settings.
        return active() ? CameraType.THIRD_PERSON_BACK : options.getCameraType();
    }

    public static void turn(double horizontal, double vertical) {
        yaw = (float) Mth.wrapDegrees(yaw + horizontal * .15);
        pitch = (float) Math.clamp(pitch + vertical * .15, -90, 90);
    }

    public static float yaw() { return yaw; }
    public static float pitch() { return pitch; }
}
