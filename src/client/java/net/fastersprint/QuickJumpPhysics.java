package net.fastersprint;

/** The upward portion of Minecraft's discrete air motion; zero means keep vanilla behavior. */
final class QuickJumpPhysics {
    private QuickJumpPhysics() { }
    static double rise(double velocity, double gravity, double drag) {
        if (!Double.isFinite(velocity) || !Double.isFinite(gravity) || !Double.isFinite(drag)
                || velocity <= 0 || gravity <= 0 || drag < 0 || drag > 1) return 0;
        double height = 0;
        for (int tick = 0; tick < 256 && velocity > 0; tick++) {
            height += velocity;
            if (height > 8) return 0; // Extreme attributes keep their original, gradual jump.
            velocity = (velocity - gravity) * drag;
            if (Math.abs(velocity) < .003) velocity = 0;
        }
        return velocity <= 0 ? height : 0;
    }
}
