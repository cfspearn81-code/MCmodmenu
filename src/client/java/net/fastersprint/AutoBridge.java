package net.fastersprint;

import com.mojang.blaze3d.platform.InputConstants;
import net.fastersprint.mixin.BridgePredictionAccessor;
import net.fastersprint.mixin.ClientLevelPredictionAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/** Normal predicted interactions, with server acknowledgement before trusting new footing. */
public final class AutoBridge {
    private static final double FOOT_INSET = 0.15;
    private static final double GROUND_MARGIN = 0.08;
    private static final double JUMP_MARGIN = 1.5;
    private static ClientLevel world;
    private static net.minecraft.client.player.LocalPlayer actor;
    private static BlockPos pending;
    private static int pendingSequence;
    private static boolean edgeHold;
    private static boolean serverPaused;
    private static boolean releasedAfterServerMove;
    private static BridgePlacementLimiter limiter = new BridgePlacementLimiter();
    private static String status = "Place underfoot";

    private AutoBridge() { }

    public static String status() { return status; }

    private static void syncWorld(Minecraft game) {
        if (world != game.level || actor != game.player) {
            world = game.level;
            actor = game.player;
            pending = null;
            edgeHold = false;
            serverPaused = false;
            releasedAfterServerMove = false;
            limiter = new BridgePlacementLimiter();
            status = "Place underfoot";
        }
    }

    private static boolean eligible(Minecraft game) {
        return !FreeCam.active() && PlayerDisplaySettings.autoBridgeEnabled() && game.player != null && game.level != null
            && game.gameMode != null && game.isWindowActive() && !game.isPaused()
            && game.player.isAlive() && !game.player.isSpectator() && !game.player.isPassenger()
            && !game.player.getAbilities().flying && !game.player.isFallFlying()
            && !game.player.isInWater() && !WallClimb.onWall(game.player);
    }

    private static boolean holdKeyDown() {
        return InputConstants.isKeyDown(PlayerDisplaySettings.autoBridgeKey());
    }

    private static boolean protecting(Minecraft game) {
        return !serverPaused && eligible(game) && (edgeHold || holdKeyDown());
    }

    public static void serverMovement() {
        var game = Minecraft.getInstance();
        syncWorld(game);
        if (!protecting(game)) return;
        serverPaused = true;
        releasedAfterServerMove = !holdKeyDown();
        edgeHold = false;
        status = "Server moved you: release key";
    }

    public static boolean stoppingForServer() {
        return serverPaused && eligible(Minecraft.getInstance()) && holdKeyDown();
    }

    public static void updateControls(Vec2 movement, boolean jump) {
        var game = Minecraft.getInstance();
        syncWorld(game);
        boolean held = holdKeyDown();
        if (!PlayerDisplaySettings.autoBridgeEnabled()) {
            edgeHold = false;
            serverPaused = false;
            releasedAfterServerMove = false;
            return;
        }
        if (serverPaused && !held) releasedAfterServerMove = true;
        if (!eligible(game)) return;
        var player = game.player;
        if (serverPaused) {
            if (!held || !releasedAfterServerMove || !player.onGround()
                    || !supported(game, inset(player.getBoundingBox()), GROUND_MARGIN)) return;
            serverPaused = false;
            releasedAfterServerMove = false;
            status = "Place underfoot";
        }
        if (held) {
            edgeHold = true;
        } else if (edgeHold && !jump && movement.lengthSquared() == 0 && player.onGround()
                && player.getDeltaMovement().horizontalDistanceSqr() < 0.0001) {
            edgeHold = false;
        }
    }

    /** Called only after vanilla has reconciled predictions with authoritative server state. */
    public static void acknowledge(ClientLevel level, int sequence) {
        if (level != world || pending == null || sequence < pendingSequence) return;
        boolean accepted = !level.getBlockState(pending).getCollisionShape(level, pending).isEmpty();
        if (!serverPaused) status = accepted ? "Place underfoot" : "Server rejected";
        pending = null;
        // Rejected actions back off instead of sending repeated placements every tick.
        var player = Minecraft.getInstance().player;
        if (accepted) limiter.accepted();
        else if (player != null) limiter.rejected(player.tickCount);
    }

    public static void tick() {
        Minecraft game = Minecraft.getInstance();
        syncWorld(game);
        if (serverPaused || !eligible(game) || !holdKeyDown()) {
            limiter.released();
            return;
        }
        var player = game.player;
        if (pending != null) {
            status = "Waiting for server";
            return; // Never resend an unacknowledged interaction, even after a long delay.
        }
        if (limiter.blocked()) {
            status = "Release key to retry";
            return;
        }
        if (game.gui.screen() != null || game.gui.overlay() != null || ClientMenu.isOpen()
                || game.options.keyUse.isDown() || player.isUsingItem()
                || !limiter.ready(player.tickCount)) return;
        if (!(player.getMainHandItem().getItem() instanceof BlockItem)) {
            status = "Select blocks";
            return;
        }
        // Use the real camera ray. Do not synthesize rotations or invisible view changes.
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(player.blockInteractionRange()));
        var ray = game.level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE,
            ClipContext.Fluid.NONE, player));
        if (ray.getType() != HitResult.Type.BLOCK) {
            status = "Aim at bridge edge";
            return;
        }
        BlockPos support = ray.getBlockPos();
        Direction face = ray.getDirection();
        BlockPos target = support.relative(face);
        int layer = BridgeSafety.footingLayer(player.getY());
        // Only place underneath/near the player, including a lower landing layer during jumps.
        if (target.getY() > layer || target.getY() < layer - 1
                || target.distToCenterSqr(player.position()) > 7
                || !game.level.getBlockState(target).canBeReplaced()) {
            status = "Aim at bridge edge";
            return;
        }
        if (game.level.getBlockState(support).hasBlockEntity()) {
            status = "Aim at solid block";
            return;
        }
        var prediction = ((ClientLevelPredictionAccessor) game.level).fastersprint$predictionHandler();
        int before = prediction.currentSequence();
        limiter.attempted(player.tickCount); // All outcomes consume the same packet budget.
        var result = game.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, ray);
        if (prediction.currentSequence() != before) {
            pending = target.immutable();
            pendingSequence = prediction.currentSequence();
            status = "Waiting for server";
        } else {
            limiter.rejected(player.tickCount);
            status = "Placement unavailable";
        }
        if (result.consumesAction()) {
            player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
        }
    }

    public static boolean isProtecting() {
        return protecting(Minecraft.getInstance());
    }

    /** Check takeoff before vanilla can apply a jump impulse. */
    public static boolean shouldStopInput(Vec2 requested, boolean jump, boolean sprint) {
        Minecraft game = Minecraft.getInstance();
        var player = game.player;
        Vec3 motion = player.getDeltaMovement();
        var step = BridgeMotion.project(requested.x, requested.y, player.getYRot(),
            motion.x, motion.y, motion.z, player.onGround(), jump, sprint || player.isSprinting());
        double safe = safeStep(game, step.x(), step.y(), step.z());
        return safe == 0 || (jump && player.onGround() && safe < 1);
    }

    /** Limit the resolved local displacement before vanilla applies it. */
    public static Vec3 guardMovement(Vec3 movement) {
        if (movement.x == 0 && movement.z == 0) return movement;
        Minecraft game = Minecraft.getInstance();
        if (!protecting(game)) return movement;
        double safe = safeStep(game, movement.x, Math.max(0, movement.y), movement.z);
        return safe >= 1 ? movement : new Vec3(movement.x * safe, movement.y, movement.z * safe);
    }

    private static AABB inset(AABB bounds) {
        return bounds.deflate(FOOT_INSET, 0, FOOT_INSET);
    }

    private static double safeStep(Minecraft game, double x, double rise, double z) {
        var player = game.player;
        AABB full = player.getBoundingBox();
        AABB feet = inset(full);
        double baseDrop = player.onGround() ? GROUND_MARGIN : JUMP_MARGIN;
        double drop = player.onGround() ? baseDrop + rise : baseDrop;
        return BridgeEdgeGuard.fraction(Math.hypot(x, z), supported(game, full, baseDrop),
            fraction -> supported(game, feet.move(x * fraction, rise * fraction, z * fraction), drop));
    }

    private static boolean supported(Minecraft game, AABB box, double maxDrop) {
        var prediction = ((ClientLevelPredictionAccessor) game.level).fastersprint$predictionHandler();
        var predicted = ((BridgePredictionAccessor) prediction).fastersprint$predictions();
        int bottom = (int) Math.floor(box.minY - maxDrop);
        int top = (int) Math.floor(box.minY - 0.001);
        for (int y = bottom; y <= top; y++) {
            for (int x = (int) Math.floor(box.minX); x <= (int) Math.floor(box.maxX); x++) {
                for (int z = (int) Math.floor(box.minZ); z <= (int) Math.floor(box.maxZ); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (pos.equals(pending) || predicted.containsKey(pos.asLong()) || !game.level.hasChunkAt(pos)) continue;
                    for (AABB shape : game.level.getBlockState(pos).getCollisionShape(game.level, pos).toAabbs()) {
                        double surface = y + shape.maxY;
                        if (surface <= box.minY + 0.01 && surface >= box.minY - maxDrop
                                && x + shape.maxX > box.minX && x + shape.minX < box.maxX
                                && z + shape.maxZ > box.minZ && z + shape.minZ < box.maxZ) return true;
                    }
                }
            }
        }
        return false;
    }
}
