package net.fastersprint;

import com.mojang.blaze3d.platform.InputConstants;
import net.fastersprint.mixin.ClientLevelPredictionAccessor;
import net.fastersprint.mixin.BridgePredictionAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
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
    private static boolean acknowledged, safetyHold;
    private static int pendingSinceTick, acknowledgedTick;
    private static final int FOOTING_SETTLE_TICKS = 10;
    private static final BridgeFootingTracker footing = new BridgeFootingTracker();
    private static BridgePlacementLimiter limiter = new BridgePlacementLimiter();
    private static String status = "Place underfoot";

    private AutoBridge() { }

    public static String status() { return status; }

    private static void syncWorld(Minecraft game) {
        if (world != game.level || actor != game.player) {
            world = game.level;
            actor = game.player;
            pending = null;
            footing.clear();
            acknowledged = false; safetyHold = false;
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

    private static boolean active(Minecraft game) {
        return eligible(game) && InputConstants.isKeyDown(PlayerDisplaySettings.autoBridgeKey());
    }

    private static boolean protecting(Minecraft game) {
        return eligible(game) && (active(game) || pending != null || safetyHold);
    }

    private static boolean predicted(ClientLevel level, BlockPos pos) {
        var handler = ((ClientLevelPredictionAccessor) level).fastersprint$predictionHandler();
        return ((BridgePredictionAccessor) handler).fastersprint$predictions().containsKey(pos.asLong());
    }

    /** Release a rejection latch only after the user stops, never while forward momentum continues. */
    public static void updateControls(net.minecraft.world.phys.Vec2 requested, boolean jump) {
        var game = Minecraft.getInstance(); syncWorld(game);
        if (!PlayerDisplaySettings.autoBridgeEnabled()) { safetyHold = false; return; }
        if (pending == null && game.player != null && !active(game) && !jump
                && requested.lengthSquared() < .000001F && game.player.onGround()
                && game.player.getDeltaMovement().horizontalDistanceSqr() < .0001) safetyHold = false;
    }

    private static void reject(net.minecraft.client.player.LocalPlayer player) {
        pending = null; acknowledged = false; safetyHold = true;
        status = "Rejected: stop to reset";
        if (player != null) limiter.rejected(player.tickCount);
    }

    /** Higher measured latency gives server corrections more time to arrive before stepping out. */
    private static int settleTicks(Minecraft game) {
        var info = game.player.connection.getPlayerInfo(game.player.getUUID());
        return Math.max(FOOTING_SETTLE_TICKS, Math.min(40, (info == null ? 0 : info.getLatency()) / 50 + 4));
    }

    /** Called only for actual predicted solid changes, including manual/secondary placements. */
    public static void predictedBlock(ClientLevel level, BlockPos position) {
        var game = Minecraft.getInstance(); syncWorld(game);
        if (level == world && PlayerDisplaySettings.autoBridgeEnabled()) {
            footing.predicted(position.asLong());
            if (protecting(game)) safetyHold = true;
        }
    }

    /** Both single-block and section packets enter this native authoritative-state method. */
    public static void serverBlock(ClientLevel level, BlockPos position, net.minecraft.world.level.block.state.BlockState state) {
        if (level != world || actor == null) return;
        footing.serverUpdate(position.asLong(), !state.getCollisionShape(level, position).isEmpty(), actor.tickCount);
    }

    public static void unload(ClientLevel level, net.minecraft.world.level.ChunkPos chunk) {
        if (level != world) return;
        footing.removeIf(key -> { var p = BlockPos.of(key); return (p.getX() >> 4) == chunk.x() && (p.getZ() >> 4) == chunk.z(); });
    }

    private static void checkFooting(Minecraft game) {
        if (game.player == null || game.level == null) return;
        int settle = settleTicks(game);
        footing.prune(game.player.tickCount, settle,
            key -> BlockPos.of(key).equals(pending) || predicted(game.level, BlockPos.of(key)),
            key -> { var p = BlockPos.of(key); return game.level.getBlockState(p).getCollisionShape(game.level, p).isEmpty(); });
        if (pending == null || !acknowledged) return;
        if (predicted(game.level, pending)) { acknowledged = false; status = "Waiting for server"; return; }
        if (game.level.getBlockState(pending).getCollisionShape(game.level, pending).isEmpty()) { reject(game.player); return; }
        if (game.player.tickCount - acknowledgedTick >= settle
                && !footing.blocked(pending.asLong(), game.player.tickCount, settle)) {
            pending = null; acknowledged = false; safetyHold = false; status = "Place underfoot";
            limiter.accepted();
        } else status = game.player.tickCount - pendingSinceTick >= 100 ? "Unconfirmed: stopped safely" : "Checking server footing";
    }

    /** Called only after vanilla has reconciled predictions with authoritative server state. */
    public static void acknowledge(ClientLevel level, int sequence) {
        if (level != world || pending == null || sequence < pendingSequence) return;
        // A newer manual prediction can still hide the server's state at this position.
        if (predicted(level, pending) || acknowledged) return;
        var player = Minecraft.getInstance().player;
        boolean accepted = !level.getBlockState(pending).getCollisionShape(level, pending).isEmpty();
        if (!accepted || player == null) { reject(player); return; }
        acknowledged = true;
        acknowledgedTick = player.tickCount;
        status = "Checking server footing";
    }

    public static void tick() {
        Minecraft game = Minecraft.getInstance();
        syncWorld(game);
        checkFooting(game);
        if (!active(game)) {
            limiter.released();
            return;
        }
        var player = game.player;
        if (pending != null) {
            status = player.tickCount - pendingSinceTick >= 100 ? "Unconfirmed: stopped safely"
                : acknowledged ? "Checking server footing" : "Waiting for server";
            return; // Never resend an unacknowledged interaction, even after a long delay.
        }
        if (limiter.blocked()) {
            status = "Release key to retry";
            return;
        }
        if (FreeCam.active() || game.gui.screen() != null || game.gui.overlay() != null || ClientMenu.isOpen()
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
        BlockPos target = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, player.getMainHandItem(), ray).getClickedPos();
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
            pendingSinceTick = player.tickCount;
            // Track even locally unsuccessful interactions; an ACK alone is never block confirmation.
            // A successful predicted change was already registered by ClientLevel.setBlock.
            if (!predicted(game.level, target)) footing.predicted(target.asLong());
            acknowledged = false; safetyHold = true;
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
        var game = Minecraft.getInstance(); syncWorld(game);
        return protecting(game);
    }

    /** Early input check; the final guard separately catches motion left over from earlier ticks. */
    public static boolean shouldStopInput(net.minecraft.world.phys.Vec2 requested, boolean jump, boolean sprint) {
        Minecraft game = Minecraft.getInstance();
        syncWorld(game);
        if (!protecting(game)) return false;
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
        if (!protecting(game) || (movement.x == 0 && movement.z == 0)) return movement;
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
        // Reject every outstanding prediction, including manual/secondary placements and replaceable-face targets.
        // Acknowledged bridge footing also stays excluded during its short rollback check.
        int settle = settleTicks(game);
        int bottom = (int) Math.floor(box.minY - maxDrop);
        int top = (int) Math.floor(box.minY - 0.001);
        for (int y = bottom; y <= top; y++) {
            for (int x = (int) Math.floor(box.minX); x <= (int) Math.floor(box.maxX); x++) {
                for (int z = (int) Math.floor(box.minZ); z <= (int) Math.floor(box.maxZ); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (pos.equals(pending) || predicted(game.level, pos)
                            || footing.blocked(pos.asLong(), game.player.tickCount, settle)) continue;
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
