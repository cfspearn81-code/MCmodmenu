package net.fastersprint;

import net.minecraft.world.phys.Vec3;

final class FreeCamMotion {
    private FreeCamMotion() { }
    static Vec3 step(float yaw, float pitch, int forward, int right, int vertical, boolean fast) {
        double y = Math.toRadians(yaw), p = Math.toRadians(pitch);
        Vec3 direction = new Vec3(-Math.sin(y)*Math.cos(p)*forward-Math.cos(y)*right,
            -Math.sin(p)*forward+vertical, Math.cos(y)*Math.cos(p)*forward-Math.sin(y)*right);
        double length = direction.length();
        return length < 0.000001 ? Vec3.ZERO : direction.scale((fast ? 1.8 : .6) / Math.max(1, length));
    }
}
