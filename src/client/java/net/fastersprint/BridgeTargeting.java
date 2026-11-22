package net.fastersprint;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/** Finds the first reachable attachment face along the requested walking path. */
final class BridgeTargeting {
    private static final double[] FOOTPRINT_OFFSETS = {-.15, .15};
    private final ClientLevel level;
    private final LocalPlayer player;
    private final Predicate<BlockPos> trusted;
    private final Vec3 eye;
    private final double rangeSquared;

    BridgeTargeting(ClientLevel level, LocalPlayer player, Predicate<BlockPos> trusted) {
        this.level = level;
        this.player = player;
        this.trusted = trusted;
        eye = player.getEyePosition();
        double reach = player.blockInteractionRange();
        rangeSquared = reach * reach;
    }

    BlockHitResult find(Vec2 input, int layer) {
        var step = BridgeMotion.project(input.x, input.y, player.getYRot(), 0, 0, 0, true, false, false);
        Vec3 direction = new Vec3(step.x(), 0, step.z()).normalize();
        Set<BlockPos> visited = new HashSet<>();
        for (int sample = 0; sample <= 6; sample++) {
            double x = player.getX() + direction.x * sample * .2;
            double z = player.getZ() + direction.z * sample * .2;
            BlockPos center = BlockPos.containing(x, layer, z);
            BlockHitResult hit = tryCell(center, visited);
            if (hit != null) return hit;
            // Check the near corners as well, so a diagonal path can place a side block first.
            for (double dx : FOOTPRINT_OFFSETS) {
                for (double dz : FOOTPRINT_OFFSETS) {
                    hit = tryCell(BlockPos.containing(x + dx, layer, z + dz), visited);
                    if (hit != null) return hit;
                }
            }
        }
        return null;
    }

    private BlockHitResult tryCell(BlockPos target, Set<BlockPos> visited) {
        if (!visited.add(target) || !level.hasChunkAt(target) || !level.getBlockState(target).canBeReplaced()) return null;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos support = target.relative(side);
            if (!trusted.test(support)) continue;
            var state = level.getBlockState(support);
            if (state.hasBlockEntity() || state.getCollisionShape(level, support).isEmpty()) continue;
            Direction face = side.getOpposite();
            // Aim a real ray at the upper middle of the outward face. A ray entering from
            // the wrong side or intercepting another block cannot make this placement.
            Vec3 point = Vec3.atCenterOf(support).add(face.getStepX() * .499, .42, face.getStepZ() * .499);
            if (eye.distanceToSqr(point) > rangeSquared) continue;
            BlockHitResult hit = level.clip(new ClipContext(eye, point, ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE, player));
            if (hit.getType() != HitResult.Type.BLOCK || !hit.getBlockPos().equals(support) || hit.getDirection() != face) continue;
            var context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, player.getMainHandItem(), hit);
            if (context.getClickedPos().equals(target) && context.canPlace()) return hit;
        }
        return null;
    }
}
