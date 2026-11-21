package net.fastersprint;

/** Applies protection to controls before both movement simulation and vanilla input sending. */
public final class BridgeControls {
    private BridgeControls() { }

    public record Keys(boolean forward, boolean backward, boolean left, boolean right,
            boolean jump, boolean sneak, boolean sprint) { }

    public static Keys protect(Keys requested, boolean active, boolean unsafeFooting) {
        if (!active) return requested;
        return new Keys(!unsafeFooting && requested.forward(), !unsafeFooting && requested.backward(),
            !unsafeFooting && requested.left(), !unsafeFooting && requested.right(),
            !unsafeFooting && requested.jump(), requested.sneak(), !unsafeFooting && requested.sprint());
    }
}
