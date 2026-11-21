package net.fastersprint;

import com.mojang.blaze3d.platform.InputConstants;
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
import net.minecraft.world.phys.Vec3;

/** Normal predicted interactions, with server acknowledgement before trusting new footing. */
public final class AutoBridge {
    private static ClientLevel world;
    private static net.minecraft.client.player.LocalPlayer actor;
    private static BlockPos pending;
    private static int pendingSequence;
    private static BridgePlacementLimiter limiter = new BridgePlacementLimiter();
    private static String status = "Place underfoot";

    private AutoBridge() { }

    public static String status() { return status; }

    private static void syncWorld(Minecraft game) {
        if (world != game.level || actor != game.player) {
            world = game.level;
            actor = game.player;
            pending = null;
            limiter = new BridgePlacementLimiter();
            status = "Place underfoot";
        }
    }

    private static boolean active(Minecraft game) {
        return PlayerDisplaySettings.autoBridgeEnabled() && game.player != null && game.level != null
            && game.gameMode != null && game.isWindowActive() && !game.isPaused()
            && InputConstants.isKeyDown(PlayerDisplaySettings.autoBridgeKey())
            && game.player.isAlive() && !game.player.isSpectator() && !game.player.isPassenger()
            && !game.player.getAbilities().flying && !game.player.isFallFlying()
            && !game.player.isInWater();
    }

    /** Called only after vanilla has reconciled predictions with authoritative server state. */
    public static void acknowledge(ClientLevel level, int sequence) {
        if (level != world || pending == null || sequence < pendingSequence) return;
        boolean accepted = !level.getBlockState(pending).getCollisionShape(level, pending).isEmpty();
        status = accepted ? "Place underfoot" : "Server rejected";
        pending = null;
        // Rejected actions back off instead of sending repeated placements every tick.
        var player = Minecraft.getInstance().player;
        if (accepted) limiter.accepted();
        else if (player != null) limiter.rejected(player.tickCount);
    }

    public static void tick() {
        Minecraft game = Minecraft.getInstance();
        syncWorld(game);
        if (!active(game)) {
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
        return active(Minecraft.getInstance());
    }

    /** Early input check; the final guard separately catches motion left over from earlier ticks. */
    public static boolean shouldStopInput(net.minecraft.world.phys.Vec2 requested, boolean jump, boolean sprint) {
        Minecraft game = Minecraft.getInstance();
        syncWorld(game);
        if (!active(game)) return false;
        var player = game.player;
        Vec3 motion = player.getDeltaMovement();
        var step = BridgeMotion.project(requested.x, requested.y, player.getYRot(),
            motion.x, motion.y, motion.z, player.onGround(), jump, sprint || player.isSprinting());
        AABB full = player.getBoundingBox();
        double baseDrop = player.onGround() ? 0.08 : 1.5;
        double drop = player.onGround() ? Math.max(baseDrop, step.y() + baseDrop) : baseDrop;
        AABB inset = full.deflate(0.10, 0, 0.10);
        return BridgeEdgeGuard.fraction(Math.hypot(step.x(), step.z()), supported(game, full, baseDrop),
            fraction -> supported(game, inset.move(step.x() * fraction,
                step.y() * fraction, step.z() * fraction), drop)) < 1;
    }

    /** Runs inside vanilla Player movement before collision resolution, catching residual momentum. */
    public static Vec3 guardMovement(Vec3 movement) {
        Minecraft game = Minecraft.getInstance();
        syncWorld(game);
        if (!active(game) || (movement.x == 0 && movement.z == 0)) return movement;
        var player = game.player;
        AABB full = player.getBoundingBox();
        AABB inset = full.deflate(0.10, 0, 0.10);
        double baseDrop = player.onGround() ? 0.08 : 1.5;
        double rise = Math.max(0, movement.y);
        double drop = player.onGround() ? Math.max(baseDrop, rise + baseDrop) : baseDrop;
        double safe = BridgeEdgeGuard.fraction(Math.hypot(movement.x, movement.z),
            supported(game, full, baseDrop),
            t -> supported(game, inset.move(movement.x * t, rise * t, movement.z * t), drop));
        if (safe >= 1) return movement;
        Vec3 velocity = player.getDeltaMovement();
        player.setDeltaMovement(0, velocity.y, 0);
        return new Vec3(movement.x * safe, movement.y, movement.z * safe);
    }

    private static boolean supported(Minecraft game, AABB box, double maxDrop) {
        // Pending blocks never count. Inset footprints reserve 0.10 blocks of the real collision box.
        int bottom = (int) Math.floor(box.minY - maxDrop);
        int top = (int) Math.floor(box.minY - 0.001);
        for (int y = bottom; y <= top; y++) {
            for (int x = (int) Math.floor(box.minX); x <= (int) Math.floor(box.maxX); x++) {
                for (int z = (int) Math.floor(box.minZ); z <= (int) Math.floor(box.maxZ); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (pos.equals(pending)) continue;
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
