package net.fastersprint;

import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

final class BedGeometry {
    static final double HEIGHT = 9.0 / 16;
    static final Direction[] DIRECTIONS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    static final int[] COLORS = new int[16];
    static { for (DyeColor color : DyeColor.values()) COLORS[color.getId()] = 0xFF000000 | color.getTextureDiffuseColor(); }
    private BedGeometry() { }
    static int encode(BlockState state) {
        if (!(state.getBlock() instanceof BedBlock bed)) return -1;
        int direction = switch (state.getValue(BedBlock.FACING)) { case NORTH -> 0; case EAST -> 1; case SOUTH -> 2; case WEST -> 3; default -> throw new IllegalArgumentException("Non-horizontal bed"); };
        return bed.getColor().getId() << 3 | direction << 1 | (state.getValue(BedBlock.PART) == BedPart.HEAD ? 1 : 0);
    }
    static int color(int type) { return type >>> 3; }
    static boolean head(int type) { return (type & 1) != 0; }
    static Direction facing(int type) { return DIRECTIONS[(type >>> 1) & 3]; }
    static boolean matches(int first, int second) { return first >= 0 && second >= 0 && (first ^ second) == 1; }
}
