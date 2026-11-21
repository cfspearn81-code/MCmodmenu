package net.fastersprint;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

/** Treat a contacted block wall as climbable; vanilla implements the actual ladder movement. */
public final class WallClimb {
    private static ClientLevel cachedWorld;
    private static LivingEntity cachedActor;
    private static AABB cachedBounds;
    private static int cachedTick;
    private static boolean cachedContact;
    private WallClimb() { }

    public static void tick() {
        if (cachedWorld == null) return;
        Minecraft game = Minecraft.getInstance();
        if (cachedWorld != game.level || cachedActor != game.player || !WallClimbSettings.enabled()
                || game.player == null || !game.player.isAlive() || FreeCam.active()) {
            cachedWorld = null; cachedActor = null; cachedBounds = null; cachedContact = false;
        }
    }

    public static boolean onWall(LivingEntity entity) {
        Minecraft game = Minecraft.getInstance();
        if (entity != game.player || game.level == null || !WallClimbSettings.enabled()
                || !entity.isAlive() || entity.isSpectator() || entity.isPassenger()
                || entity.isFallFlying() || entity.isInWater() || entity.isInLava()
                || game.player.getAbilities().flying || FreeCam.active()) return false;
        AABB bounds = entity.getBoundingBox();
        // Reuse repeated queries at one position within the tick; refresh after physics/pose changes.
        if (cachedWorld == game.level && cachedActor == entity && cachedBounds == bounds && cachedTick == entity.tickCount) return cachedContact;
        cachedWorld = game.level; cachedActor = entity; cachedBounds = bounds; cachedTick = entity.tickCount;
        cachedContact = false;
        // Only side contact, excluding a floor/ceiling touching the normal player box.
        AABB sides = bounds.deflate(0, .05, 0).inflate(.04, 0, .04);
        for (var shape : game.level.getBlockCollisions(entity, sides)) {
            if (!shape.isEmpty()) { cachedContact = true; break; }
        }
        return cachedContact;
    }
}
