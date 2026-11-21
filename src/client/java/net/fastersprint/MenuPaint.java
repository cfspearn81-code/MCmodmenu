package net.fastersprint;

import net.minecraft.client.gui.GuiGraphicsExtractor;

final class MenuPaint {
    private MenuPaint() { }
    static void rounded(GuiGraphicsExtractor g, int x, int y, int w, int h, int radius, int color) {
        int r = Math.max(0, Math.min(radius, Math.min(w, h) / 2));
        if (r == 0) { g.fill(x, y, x + w, y + h, color); return; }
        g.fill(x, y + r, x + w, y + h - r, color);
        for (int row = 0; row < r; row++) {
            double dy = r - row - 0.5;
            int inset = (int) Math.ceil(r - Math.sqrt(r * r - dy * dy));
            g.fill(x + inset, y + row, x + w - inset, y + row + 1, color);
            g.fill(x + inset, y + h - row - 1, x + w - inset, y + h - row, color);
        }
    }
    static void circle(GuiGraphicsExtractor g, float x, float y, float radius, int color) {
        for (int row = (int) Math.floor(y - radius); row <= (int) Math.ceil(y + radius); row++) {
            double dy = row + 0.5 - y;
            if (Math.abs(dy) > radius) continue;
            double extent = Math.sqrt(radius * radius - dy * dy);
            int left = (int) Math.ceil(x - extent - 0.5);
            int right = (int) Math.floor(x + extent - 0.5) + 1;
            if (right > left) g.fill(left, row, right, row + 1, color);
        }
    }
    static int mix(int a, int b, float t) {
        int result = 0;
        for (int shift = 0; shift <= 24; shift += 8) {
            int channel = Math.round(((a >>> shift) & 255) * (1 - t) + ((b >>> shift) & 255) * t);
            result |= channel << shift;
        }
        return result;
    }
}
