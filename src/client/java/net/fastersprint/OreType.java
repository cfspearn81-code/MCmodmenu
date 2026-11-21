package net.fastersprint;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Copper is deliberately absent; variants share one filter and color. */
public enum OreType {
    COAL("Coal", 0xFF9399A5),
    IRON("Iron", 0xFFE6C2A6),
    GOLD("Gold", 0xFFFFD34E),
    REDSTONE("Redstone", 0xFFFF535F),
    LAPIS("Lapis", 0xFF567DFF),
    DIAMOND("Diamond", 0xFF50E6ED),
    EMERALD("Emerald", 0xFF59E689),
    QUARTZ("Quartz", 0xFFFFF0DD),
    NETHERITE("Netherite", 0xFFBD8DDE);

    public final String label;
    public final int color;

    OreType(String label, int color) {
        this.label = label;
        this.color = color;
    }

    public static OreType of(BlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE) return COAL;
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) return IRON;
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.NETHER_GOLD_ORE) return GOLD;
        if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) return REDSTONE;
        if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE) return LAPIS;
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) return DIAMOND;
        if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) return EMERALD;
        if (block == Blocks.NETHER_QUARTZ_ORE) return QUARTZ;
        if (block == Blocks.ANCIENT_DEBRIS) return NETHERITE;
        return null;
    }
}
