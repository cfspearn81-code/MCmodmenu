package net.fastersprint;

public final class ColorMath {
    private ColorMath() { }
    public static int rgb(float hue, float saturation, float value) {
        float h = (hue - (float)Math.floor(hue)) * 6;
        float f = h - (int)h, p = value * (1 - saturation), q = value * (1 - saturation * f), t = value * (1 - saturation * (1 - f));
        float r, g, b;
        switch ((int)h) {
            case 0 -> { r=value;g=t;b=p; } case 1 -> { r=q;g=value;b=p; }
            case 2 -> { r=p;g=value;b=t; } case 3 -> { r=p;g=q;b=value; }
            case 4 -> { r=t;g=p;b=value; } default -> { r=value;g=p;b=q; }
        }
        return 0xFF000000 | Math.round(r * 255) << 16 | Math.round(g * 255) << 8 | Math.round(b * 255);
    }
    public static float unit(float value) {
        if (!Float.isFinite(value)) throw new IllegalArgumentException("Color components must be finite");
        return Math.clamp(value, 0, 1);
    }
}
