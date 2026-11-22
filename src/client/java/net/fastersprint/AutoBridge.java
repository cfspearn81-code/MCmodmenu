package net.fastersprint;

import com.mojang.blaze3d.platform.InputConstants;
import net.fastersprint.mixin.ClientLevelPredictionAccessor;
import net.fastersprint.mixin.BridgePredictionAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/** Normal predicted interactions, with server acknowledgement before trusting new footing. */
public final class AutoBridge {
    private static ClientLevel world;
    private static net.minecraft.client.player.LocalPlayer actor;
    private static BlockPos pending;
    private static int pendingSequence;
    private static boolean acknowledged, safetyHold, serverPaused, releasedAfterCorrection;
    private static int pendingSinceTick, acknowledgedTick;
    // A server can acknowledge a block and then remove it. Keep the full wait for every placement.
    static final int SETTLE_TICKS = 12;
    private static final int ACK_SETTLE_TICKS = 2;
    private static final int DELAYED_ACK_TICKS = 8;
    private static int walkingLayer;
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
            walkingLayer = actor == null ? 0 : (int) Math.floor(actor.getY() - .001);
            acknowledged = false; safetyHold = false;
            serverPaused = false; releasedAfterCorrection = false;
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
        return !serverPaused && eligible(game) && holdKeyDown();
    }

    private static boolean holdKeyDown() {
        return InputConstants.isKeyDown(PlayerDisplaySettings.autoBridgeKey());
    }

    private static boolean protecting(Minecraft game) {
        return !serverPaused && eligible(game) && (holdKeyDown() || pending != null || safetyHold);
    }

    /** The server has already applied its position or velocity. Stop bridge automation until the player rearms it. */
    public static void serverMovement() {
        var game = Minecraft.getInstance();
        syncWorld(game);
        if (!protecting(game)) return;
        serverPaused = true;
        releasedAfterCorrection = !holdKeyDown();
        safetyHold = false;
        status = "Server moved you: release key";
        game.player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "Auto Bridge paused. Release the bridge key and return to solid ground."));
    }

    /** While the bridge key remains held after a correction, suppress continued walking input, not server velocity. */
    public static boolean stoppingForServer() {
        return serverPaused && eligible(Minecraft.getInstance()) && holdKeyDown();
    }

    private static boolean predicted(ClientLevel level, BlockPos pos) {
        var handler = ((ClientLevelPredictionAccessor) level).fastersprint$predictionHandler();
        return ((BridgePredictionAccessor) handler).fastersprint$predictions().containsKey(pos.asLong());
    }

    /** Keep the edge guard until the user releases the key and comes to rest on trusted ground. */
    public static void updateControls(Vec2 requested, boolean jump) {
        var game = Minecraft.getInstance();
        syncWorld(game);
        if (!PlayerDisplaySettings.autoBridgeEnabled()) {
            safetyHold = false;
            serverPaused = false;
            releasedAfterCorrection = false;
            return;
        }
        if (serverPaused) {
            if (!holdKeyDown()) {
                releasedAfterCorrection = true;
                return;
            }
            if (!releasedAfterCorrection || !eligible(game) || !game.player.onGround()
                    || !supported(game, game.player.getBoundingBox().deflate(.25, 0, .25), .08)) return;
            serverPaused = false;
            releasedAfterCorrection = false;
        }
        if (active(game)) { safetyHold = true; return; }
        if (safetyHold && pending == null && game.player != null && !jump
                && requested.lengthSquared() < .000001F && game.player.onGround()
                && game.player.getDeltaMovement().horizontalDistanceSqr() < .0001
                && supported(game, game.player.getBoundingBox().deflate(.15, 0, .15), .08)) safetyHold = false;
    }

    private static void reject(net.minecraft.client.player.LocalPlayer player) {
        pending = null; acknowledged = false; safetyHold = true;
        status = "Rejected: stop to reset";
        if (player != null) limiter.rejected(player.tickCount);
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
        if (footing.serverUpdate(position.asLong(), !state.getCollisionShape(level, position).isEmpty(), actor.tickCount)) {
            safetyHold = true;
            // The normal pending check handles its own rejection once; older footing needs backoff too.
            if (!position.equals(pending)) limiter.rejected(actor.tickCount);
        }
    }

    public static void unload(ClientLevel level, net.minecraft.world.level.ChunkPos chunk) {
        if (level != world) return;
        footing.removeIf(key -> { var p = BlockPos.of(key); return (p.getX() >> 4) == chunk.x() && (p.getZ() >> 4) == chunk.z(); });
    }

    private static void checkFooting(Minecraft game) {
        if (game.player == null || game.level == null) return;
        if (pending != null && !game.level.hasChunkAt(pending)) reject(game.player);
        footing.prune(game.player.tickCount, SETTLE_TICKS,
            key -> BlockPos.of(key).equals(pending) || predicted(game.level, BlockPos.of(key)),
            key -> { var p = BlockPos.of(key); return game.level.getBlockState(p).getCollisionShape(game.level, p).isEmpty(); });
        if (pending == null || !acknowledged) return;
        if (predicted(game.level, pending)) { acknowledged = false; status = "Waiting for server"; return; }
        if (game.level.getBlockState(pending).getCollisionShape(game.level, pending).isEmpty()) { reject(game.player); return; }
        // Only overlap the waits when the solid update clearly preceded a delayed acknowledgement.
        // When the messages arrive close together, the acknowledgement stays part of the full rollback window.
        boolean delayedAck = footing.confirmed(pending.asLong(), acknowledgedTick, DELAYED_ACK_TICKS);
        int acknowledgementWait = delayedAck ? ACK_SETTLE_TICKS : SETTLE_TICKS;
        if (game.player.tickCount - acknowledgedTick >= acknowledgementWait
                && footing.confirmed(pending.asLong(), game.player.tickCount, SETTLE_TICKS)) {
            footing.approve(pending.asLong(), game.player.tickCount);
            pending = null; acknowledged = false; status = "Place underfoot";
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
        if (game.player != null && game.player.onGround()) walkingLayer = (int) Math.floor(game.player.getY() - .001);
        if (!active(game)) limiter.released();
        if (serverPaused) status = releasedAfterCorrection ? "Resume from solid ground" : "Server moved you: release key";
    }

    /** Use the current movement request before vanilla applies it this tick. */
    public static void place(Vec2 requestedMovement) {
        var game = Minecraft.getInstance();
        if (!active(game)) {
            return;
        }
        safetyHold = true;
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
        if (requestedMovement.lengthSquared() < .000001F) {
            status = "Move to bridge";
            return;
        }
        int currentLayer = (int) Math.floor(player.getY() - .001);
        if (currentLayer < walkingLayer || currentLayer > walkingLayer + 1) return;
        var ray = new BridgeTargeting(game.level, player, pos -> trusted(game, pos)).find(requestedMovement, walkingLayer);
        if (ray == null) {
            status = "No reachable bridge face";
            return;
        }
        BlockPos target = ray.getBlockPos().relative(ray.getDirection());
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
        AABB inset = full.deflate(0.15, 0, 0.15);
        double safe = BridgeEdgeGuard.fraction(Math.hypot(step.x(), step.z()), supported(game, full, baseDrop),
            fraction -> supported(game, inset.move(step.x() * fraction,
                step.y() * fraction, step.z() * fraction), drop));
        // Let a walking step approach the edge. The movement guard clips the actual step.
        // An unsafe takeoff must be blocked before it creates a jump impulse.
        return safe == 0 || (jump && player.onGround() && safe < 1);
    }

    /** Runs inside vanilla Player movement before collision resolution, catching residual momentum. */
    public static Vec3 guardMovement(Vec3 movement) {
        Minecraft game = Minecraft.getInstance();
        syncWorld(game);
        if (!protecting(game) || (movement.x == 0 && movement.z == 0)) return movement;
        var player = game.player;
        AABB full = player.getBoundingBox();
        AABB inset = full.deflate(0.15, 0, 0.15);
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

    private static boolean trusted(Minecraft game, BlockPos pos) {
        return !pos.equals(pending) && game.level.hasChunkAt(pos) && !predicted(game.level, pos)
            && !footing.blocked(pos.asLong(), game.player.tickCount, SETTLE_TICKS);
    }

    private static boolean supported(Minecraft game, AABB box, double maxDrop) {
        int bottom = (int) Math.floor(box.minY - maxDrop);
        int top = (int) Math.floor(box.minY - 0.001);
        for (int y = bottom; y <= top; y++) {
            for (int x = (int) Math.floor(box.minX); x <= (int) Math.floor(box.maxX); x++) {
                for (int z = (int) Math.floor(box.minZ); z <= (int) Math.floor(box.maxZ); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!trusted(game, pos)) continue;
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
