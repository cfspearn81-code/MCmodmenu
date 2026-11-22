package check;

import check.mixin.MouseAccess;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.InputConstants;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fastersprint.*;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

public final class FreeLookCheck {
    public static boolean focus = true, aimHold;
    public static int heldKey = -1;
    private static boolean connecting, configured, finished, auditMovement;
    private static int warmup = 100, stage, movementPackets;
    private static final List<String> results = new ArrayList<>();
    private static Vec3 walkingStart;
    private static RemotePlayer aimTarget;

    private static Minecraft game() {
        return Minecraft.getInstance();
    }

    private static Camera camera() {
        return game().gameRenderer.mainCamera();
    }

    private static void command(String text) {
        game().player.connection.sendCommand(text);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void pass(String message) {
        results.add("PASS " + message);
        System.out.println("FREELOOK PASS " + message);
    }

    private static void capture(String name) {
        Screenshot.grab(new java.io.File("/tmp/freelook-live"), name,
            game().gameRenderer.mainRenderTarget(), 1, ignored -> {});
    }

    private static double angle(double value) {
        value %= 360;
        if (value > 180) value -= 360;
        if (value < -180) value += 360;
        return value;
    }

    private static void look(float yaw, float pitch) {
        game().player.setYRot(yaw);
        game().player.setXRot(pitch);
        game().player.setOldPosAndRot();
    }

    private static void mouse(double x, double y) {
        var mouse = (MouseAccess) game().mouseHandler;
        mouse.check$setX(x);
        mouse.check$setY(y);
        mouse.check$turn(.05);
        mouse.check$setX(0);
        mouse.check$setY(0);
    }

    private static void key(int scan, int symbol) {
        var game = game();
        game.keyboardHandler.keyPress(game.getWindow().handle(), InputConstants.PRESS, new KeyEvent(scan, symbol, 0));
        game.keyboardHandler.keyPress(game.getWindow().handle(), InputConstants.RELEASE, new KeyEvent(scan, symbol, 0));
    }

    private static void click(int x, int y) {
        var window = game().getWindow();
        float scale = Math.min(1F, Math.min((window.getGuiScaledWidth() - 20F) / 460,
            (window.getGuiScaledHeight() - 20F) / 410));
        ClientMenu.click((window.getGuiScaledWidth() - 460 * scale) / 2 + x * scale,
            (window.getGuiScaledHeight() - 410 * scale) / 2 + y * scale);
    }

    private static void checkBody(float yaw, float pitch) {
        var player = game().player;
        check(Math.abs(angle(player.getYRot() - yaw)) < .01 && Math.abs(player.getXRot() - pitch) < .01,
            "body rotation changed to " + player.getYRot() + ", " + player.getXRot());
    }

    private static void checkOrbit() {
        var player = game().player;
        var offset = player.getEyePosition().subtract(camera().position());
        var forward = camera().forwardVector();
        check(camera().isDetached(), "camera does not use third person");
        check(Math.abs(offset.length() - 4) < .3, "camera does not keep orbit distance " + offset);
        check(offset.normalize().dot(new Vec3(forward.x(), forward.y(), forward.z())) > .998,
            "camera does not look back toward player");
    }

    public static void packet(Packet<?> packet) {
        if (!auditMovement || !(packet instanceof ServerboundMovePlayerPacket move)) return;
        movementPackets++;
        check(!move.hasRotation() || (Math.abs(angle(move.getYRot(0))) < .01 && Math.abs(move.getXRot(0)) < .01),
            "movement packet sent orbit yaw/pitch");
    }

    private static void configure() throws Exception {
        var game = game();
        game.options.pauseOnLostFocus = false;
        game.options.setCameraType(CameraType.FIRST_PERSON);
        game.options.renderDistance().set(3);
        game.options.guiScale().set(2);
        game.options.sensitivity().set(.5);
        game.options.invertMouseX().set(false);
        game.options.invertMouseY().set(false);
        game.options.smoothCamera = false;
        PlayerDisplaySettings.setAutoBridgeEnabled(false);
        PlayerDisplaySettings.setAutoHitEnabled(false);
        PlayerDisplaySettings.setEnabled(false);
        PlayerDisplaySettings.setGuiMoveEnabled(false);
        XraySettings.setEnabled(false);
        BedEspSettings.setEnabled(false);
        QuickFallSettings.setEnabled(false);
        QuickJumpSettings.setEnabled(false);
        WallClimbSettings.setEnabled(false);
        TintSettings.SKY.setEnabled(false);
        AimLockSettings.setEnabled(false);
        AimLockSettings.setKey(InputConstants.KEY_R);
        FreeCam.disable();
        FreeLookSettings.setEnabled(true);
        FreeLookSettings.setKey(InputConstants.KEY_LALT);
        command("gamemode survival BridgeLive");
        command("execute in minecraft:overworld run tp BridgeLive -255.5 120 -255.5 0 0");
        command("execute in minecraft:overworld run forceload add -272 -272 -240 -240");
        command("time set 6000");
        command("weather clear");
    }

    public static void tick() {
        if (finished) return;
        var game = game();
        if (game.player == null || game.level == null) {
            if (!connecting && game.gui.overlay() == null) {
                connecting = true;
                net.minecraft.client.gui.screens.ConnectScreen.startConnecting(game.gui.screen(), game,
                    new ServerAddress("127.0.0.1", 25568),
                    new ServerData("Free Look test", "127.0.0.1:25568", ServerData.Type.OTHER), false, null);
            }
            return;
        }
        try {
            if (!configured) {
                configured = true;
                configure();
            }
            if (warmup-- > 0 || game.gui.overlay() != null) return;
            stage++;
            switch (stage) {
                case 1 -> {
                    command("fill -266 119 -266 -242 119 -242 stone");
                    command("fill -266 120 -266 -242 125 -242 air");
                    command("fill -260 120 -258 -251 125 -258 stone");
                }
                case 10 -> {
                    key(InputConstants.KEY_P, 'p');
                    check(ClientMenu.isOpen(), "P menu did not open");
                    for (char c : "freelook".toCharArray()) key(InputConstants.KEY_A + c - 'a', c);
                    click(180, 109);
                    check(!FreeLookSettings.enabled(), "Search Free Look card did not toggle");
                    click(40, 120);
                    click(180, 325);
                    check(FreeLookSettings.enabled(), "Visuals Free Look card did not toggle");
                    click(350, 349);
                    check(ClientMenu.capturesMovement(), "Free Look hold key control did not capture");
                    key(InputConstants.KEY_F7, 0);
                    check(FreeLookSettings.key() == InputConstants.KEY_F7, "Free Look did not bind F7");
                    click(350, 349);
                    key(InputConstants.KEY_LALT, 0);
                    check(FreeLookSettings.key() == InputConstants.KEY_LALT && !ClientMenu.capturesMovement(),
                        "Free Look did not restore Left Alt binding");
                    pass("Search and Visuals toggle Free Look and the hold key control changes its binding");
                }
                case 13 -> capture("free-look-menu.png");
                case 15 -> {
                    ClientMenu.scroll(-1);
                    click(180, 325);
                    check(TintSettings.SKY.enabled(), "Sky Tint unreachable after scrolling Visuals");
                    click(180, 325);
                    check(!TintSettings.SKY.enabled(), "Sky Tint toggle did not reset");
                    key(InputConstants.KEY_ESCAPE, 27);
                    pass("Sky Tint remains reachable by scrolling Visuals");
                }
                case 20 -> {
                    check(!FreeLook.active() && !camera().isDetached(), "Free Look engaged without hold key");
                    look(0, 0);
                    heldKey = InputConstants.KEY_LALT;
                }
                case 25 -> {
                    check(FreeLook.active(), "Left Alt did not activate Free Look");
                    checkOrbit();
                    check(game.options.getCameraType() == CameraType.FIRST_PERSON, "saved camera perspective changed");
                    check(camera().position().z < -258.5, "wall compressed camera boom");
                    checkBody(0, 0);
                    pass("Left Alt created a four-block third-person orbit through a solid wall");
                    auditMovement = true;
                }
                case 26 -> {
                    capture("free-look-wall.png");
                    mouse(600, 150);
                }
                case 30 -> {
                    check(Math.abs(angle(camera().yRot() - 90)) < 1 && Math.abs(camera().xRot() - 22.5) < 1,
                        "mouse did not orbit yaw/pitch " + camera().yRot() + ", " + camera().xRot());
                    checkBody(0, 0);
                    checkOrbit();
                    pass("mouse yaw and pitch orbit independently of player yaw and pitch");
                    walkingStart = game.player.position();
                    game.options.keyUp.setDown(true);
                }
                case 35 -> capture("free-look-orbit.png");
                case 44 -> {
                    game.options.keyUp.setDown(false);
                    var walked = game.player.position().subtract(walkingStart);
                    check(walked.z > 1 && Math.abs(walked.x) < .05, "walking follows camera rather than body " + walked);
                    checkOrbit();
                    checkBody(0, 0);
                    check(movementPackets > 0, "did not see actual movement packets");
                    pass("forward input kept the player's original direction and the camera followed");
                    mouse(1200, -2000);
                }
                case 49 -> {
                    check(Math.abs(angle(camera().yRot() - 270)) < 1 && Math.abs(camera().xRot() + 90) < 1,
                        "orbit does not support reverse direction or pole pitch");
                    checkBody(0, 0);
                    pass("orbit turns past 180 degrees horizontally and reaches the vertical pole");
                    auditMovement = false;
                    heldKey = -1;
                }
                case 54 -> {
                    check(!FreeLook.active() && !camera().isDetached() && game.options.getCameraType() == CameraType.FIRST_PERSON,
                        "release did not restore first person");
                    checkBody(0, 0);
                    mouse(200, 0);
                }
                case 59 -> {
                    checkBody(30, 0);
                    pass("release restored first person and normal player mouse rotation");
                    game.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
                    FreeLookSettings.setKey(InputConstants.KEY_F7);
                    heldKey = InputConstants.KEY_LALT;
                }
                case 64 -> {
                    check(!FreeLook.active(), "rebinding did not retire old key");
                    heldKey = InputConstants.KEY_F7;
                }
                case 69 -> {
                    check(FreeLook.active(), "rebound F7 failed");
                    checkOrbit();
                    heldKey = -1;
                }
                case 74 -> {
                    check(!FreeLook.active() && game.options.getCameraType() == CameraType.THIRD_PERSON_FRONT
                        && camera().isDetached() && Math.abs(angle(camera().yRot() - 210)) < 1,
                        "release did not restore previously selected front-facing view");
                    pass("rebinding worked and release restored the selected third-person front view");
                    game.options.setCameraType(CameraType.FIRST_PERSON);
                    FreeLookSettings.setKey(InputConstants.KEY_LALT);
                    heldKey = InputConstants.KEY_LALT;
                }
                case 79 -> {
                    check(FreeLook.active(), "did not reacquire after rebinding");
                    FreeCam.enable();
                    mouse(200, 100);
                }
                case 84 -> {
                    check(!FreeLook.active() && FreeCam.active(), "Free Look interfered with Free Cam priority");
                    checkBody(30, 0);
                    check(Math.abs(angle(camera().yRot() - 60)) < 1 && Math.abs(camera().xRot() - 15) < 1,
                        "Free Cam did not receive mouse movement");
                    pass("Free Cam kept priority for camera and mouse input");
                    FreeCam.disable();
                    heldKey = -1;
                }
                case 89 -> {
                    look(0, 0);
                    aimTarget = new RemotePlayer(game.level, new GameProfile(UUID.randomUUID(), "FocusTest"));
                    aimTarget.setId(-6_403_700);
                    aimTarget.setPos(game.player.position().add(2, 0, 8));
                    aimTarget.setOldPosAndRot();
                    game.level.addEntity(aimTarget);
                    AimLockSettings.setEnabled(true);
                    heldKey = InputConstants.KEY_LALT;
                    aimHold = true;
                }
                case 94 -> {
                    check(FreeLook.active(), "orbit didn't begin during simultaneous Aim Lock hold");
                    checkBody(0, 0);
                    pass("Aim Lock did not change body direction while Free Look was held");
                    heldKey = -1;
                }
                case 99 -> {
                    check(Math.abs(angle(game.player.getYRot())) > 5, "Aim Lock did not resume after release");
                    aimHold = false;
                    AimLockSettings.setEnabled(false);
                    aimTarget.discard();
                    FreeLookSettings.setEnabled(false);
                    heldKey = InputConstants.KEY_LALT;
                }
                case 104 -> {
                    check(!FreeLook.active(), "disabled module still activates");
                    FreeLookSettings.setEnabled(true);
                    look(0, 0);
                }
                case 109 -> {
                    check(FreeLook.active(), "module did not activate after re-enable");
                    focus = false;
                }
                case 114 -> {
                    check(!FreeLook.active() && game.options.getCameraType() == CameraType.FIRST_PERSON,
                        "loss of focus did not restore perspective");
                    focus = true;
                    heldKey = -1;
                }
                case 119 -> {
                    heldKey = InputConstants.KEY_LALT;
                    key(InputConstants.KEY_P, 'p');
                }
                case 124 -> {
                    check(ClientMenu.isOpen() && !FreeLook.active(), "open module menu allowed Free Look");
                    heldKey = -1;
                    key(InputConstants.KEY_ESCAPE, 27);
                    pass("disabled module, focus loss and open menu all kept or restored the normal view");
                }
                case 129 -> {
                    finished = true;
                    FreeLookSettings.setKey(InputConstants.KEY_LALT);
                    Files.writeString(Path.of("/tmp/freelook-live/results.txt"), String.join("\n", results) + "\n");
                    System.out.println("FREELOOK ALL CHECKS COMPLETE (movement packets: " + movementPackets + ")");
                    game.stop();
                }
                default -> { }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Free Look fixture stage " + stage, e);
        }
    }
}
