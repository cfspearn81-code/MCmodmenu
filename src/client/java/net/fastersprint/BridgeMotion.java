package net.fastersprint;

/** One-tick movement projection; refresh from current controls so turns do not use stale routes. */
public final class BridgeMotion {
    private BridgeMotion() { }

    public record Step(double x, double y, double z) { }

    public static Step project(double sideways, double forward, double yawDegrees,
            double velocityX, double velocityY, double velocityZ,
            boolean onGround, boolean jump, boolean sprint) {
        double length = Math.hypot(sideways, forward);
        if (length > 1) { sideways /= length; forward /= length; }
        double yaw = Math.toRadians(yawDegrees);
        double acceleration = onGround ? (sprint ? 0.13 : 0.10) : 0.02;
        double x = sideways * Math.cos(yaw) - forward * Math.sin(yaw);
        double z = forward * Math.cos(yaw) + sideways * Math.sin(yaw);
        double y = onGround && jump ? 0.42 : Math.max(0, velocityY);
        double sprintJumpX = onGround && jump && sprint ? -Math.sin(yaw) * 0.2 : 0;
        double sprintJumpZ = onGround && jump && sprint ? Math.cos(yaw) * 0.2 : 0;
        return new Step(velocityX + x * acceleration + sprintJumpX, y,
            velocityZ + z * acceleration + sprintJumpZ);
    }
}
