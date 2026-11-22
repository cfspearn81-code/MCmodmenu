package net.spawnnpc;

import com.mojang.authlib.GameProfile;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class NpcManager {
    private static final Map<Integer, TestNpc> npcs = new LinkedHashMap<>();
    private static ClientLevel world;
    private static LocalPlayer owner;
    private static int nextId = -1_000_000_000, serial;
    private NpcManager() { }

    public static void tick() {
        var game = Minecraft.getInstance();
        if (world != game.level || owner != game.player) {
            for (var npc : npcs.values()) if (world != null && world.getEntity(npc.getId()) == npc)
                world.removeEntity(npc.getId(), Entity.RemovalReason.DISCARDED);
            npcs.clear(); world = game.level; owner = game.player; serial = 0;
        }
        npcs.values().removeIf(npc -> npc.isRemoved() || world == null || world.getEntity(npc.getId()) != npc);
    }

    private static void message(String text) {
        var player = Minecraft.getInstance().player;
        if (player != null) player.sendSystemMessage(Component.literal("[Spawn NPC] " + text));
    }

    /** Intercept only this exact command, before Minecraft sends anything to the server. */
    public static boolean command(String input) {
        String command = input.strip();
        int end = 0;
        while (end < command.length() && !Character.isWhitespace(command.charAt(end))) end++;
        if (!command.substring(0, end).equals("spawnnpc")) return false;
        tick();
        String argument = command.substring(end).strip();
        boolean knockback = argument.equals("kb");
        if (!argument.isEmpty() && !knockback) { message("Usage: /spawnnpc [kb]"); return true; }
        if (world == null || owner == null) { message("Join a world first."); return true; }
        if (npcs.size() >= 64) { message("Limit reached (64). Hit a dummy with a diamond block to remove it."); return true; }
        int number = serial + 1;
        String suffix = "_NPC" + number, name = owner.getGameProfile().name();
        name = name.substring(0, Math.min(name.length(), 16 - suffix.length())) + suffix;
        var npc = new TestNpc(world, new GameProfile(UUID.randomUUID(), name), owner, knockback);
        double yaw = Math.toRadians(owner.getYRot());
        boolean positioned = false;
        positions: for (double offset : new double[]{0, -30, 30, -60, 60}) for (double distance : new double[]{2.5, 2, 1.5, 1}) {
            double direction = yaw + Math.toRadians(offset);
            double x = owner.getX() - Math.sin(direction) * distance, z = owner.getZ() + Math.cos(direction) * distance;
            var ground = world.clip(new ClipContext(new Vec3(x, owner.getY() + 1.5, z),
                new Vec3(x, owner.getY() - 3, z), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
            if (ground.getType() != HitResult.Type.BLOCK) continue;
            npc.setPos(x, ground.getLocation().y, z);
            if (world.noCollision(npc, npc.getBoundingBox()) && npcs.values().stream()
                    .noneMatch(other -> other.getBoundingBox().intersects(npc.getBoundingBox()))) {
                positioned = true; break positions;
            }
        }
        if (!positioned) { message("No clear floor nearby. Face an open area and try again."); return true; }
        while (world.getEntity(nextId) != null) nextId--;
        npc.setId(nextId--);
        if (knockback) npc.setOnGround(true);
        npc.setYRot(owner.getYRot() + 180); npc.setYHeadRot(npc.getYRot()); npc.setYBodyRot(npc.getYRot());
        npc.yHeadRotO = npc.yBodyRotO = npc.getYRot(); npc.setOldPosAndRot();
        for (var slot : new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND,
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET})
            npc.setItemSlot(slot, owner.getItemBySlot(slot).copy());
        npcs.put(npc.getId(), npc); world.addEntity(npc); serial = number;
        message("Spawned " + name + (knockback ? " (knockback enabled)" : "") + ". Hit it with a diamond block to remove it.");
        return true;
    }

    public static boolean owns(Entity entity) {
        return entity != null && entity.level() == world && npcs.get(entity.getId()) == entity
            && world.getEntity(entity.getId()) == entity;
    }

    /** Handle local hits only; never send an attack against a nonexistent server entity. */
    public static boolean attack(Player player, Entity entity) {
        if (!owns(entity)) return entity instanceof TestNpc; // Swallow clicks on a just-removed local dummy.
        if (player != Minecraft.getInstance().player) return true;
        if (player.getMainHandItem().is(Items.DIAMOND_BLOCK)) {
            npcs.remove(entity.getId()); world.removeEntity(entity.getId(), Entity.RemovalReason.DISCARDED);
            message("Removed " + entity.getName().getString() + ".");
        } else ((TestNpc) entity).reactToHit(player);
        player.resetAttackStrengthTicker();
        return true;
    }
}
