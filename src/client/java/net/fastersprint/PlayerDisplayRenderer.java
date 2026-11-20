package net.fastersprint;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public final class PlayerDisplayRenderer {
    private static final int BOX_COLOR = 0xFF00FF00;
    private static final int TRACER_COLOR = 0xFFFF3030;
    private static final int NAME_COLOR = 0xFFFFFFFF;
    private static final float MINIMUM_W = Camera.PROJECTION_Z_NEAR;

    private PlayerDisplayRenderer() {
    }

    public static boolean shouldHighlight(Entity entity) {
        Minecraft game = Minecraft.getInstance();
        if (!PlayerDisplaySettings.enabled() || game.player == null || game.level == null
                || game.gui.hud.isHidden() || game.gui.screen() != null) {
            return false;
        }
        if (!(entity instanceof Player player) || player == game.player
                || player.level() != game.level || !player.isAlive()
                || player.isRemoved() || player.isSpectator()) {
            return false;
        }
        double range = PlayerDisplaySettings.range();
        return game.player.distanceToSqr(player) <= range * range;
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        Minecraft game = Minecraft.getInstance();
        if (!PlayerDisplaySettings.enabled() || game.player == null || game.level == null
                || game.gui.hud.isHidden() || game.gui.screen() != null) {
            return;
        }
        Camera camera = game.gameRenderer.mainCamera();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        if (!camera.isInitialized() || width < 64 || height < 64) {
            return;
        }

        Matrix4f projection = camera.getViewRotationProjectionMatrix(new Matrix4f());
        Vec3 cameraPosition = camera.position();
        float partialTick = delta.getGameTimeDeltaPartialTick(false);
        List<AbstractClientPlayer> players = new ArrayList<>(game.level.players());
        players.removeIf(player -> !shouldHighlight(player));
        boolean showNames = shouldShowNames(players.size(), PlayerDisplaySettings.nameLimit());
        players.sort(Comparator.comparingDouble((AbstractClientPlayer player) -> game.player.distanceToSqr(player)).reversed());
        List<LabelBounds> labels = new ArrayList<>();
        for (AbstractClientPlayer player : players) {
            AABB bounds = player.getInterpolatedBoundingBox(partialTick);
            Vector4f[] corners = projectCorners(bounds, cameraPosition, projection);
            double distance = Math.sqrt(game.player.distanceToSqr(player));
            String name = showNames ? player.getName().getString() : "";
            String distanceText = Math.round(distance) + " blocks";
            Vector4f feet = project(new Vec3(
                bounds.getCenter().x, bounds.minY, bounds.getCenter().z), cameraPosition, projection);
            if (isOnScreen(corners)) {
                drawPlayer(graphics, bounds, corners, feet, cameraPosition, projection,
                    name, distanceText, width, height, labels);
            } else {
                drawOffscreenMarker(graphics, feet, name, distanceText, width, height, labels);
            }
        }
    }

    private static boolean shouldShowNames(int selectedCount, int limit) {
        return selectedCount <= limit;
    }

    private static void drawPlayer(
            GuiGraphicsExtractor graphics, AABB bounds, Vector4f[] corners, Vector4f feet,
            Vec3 cameraPosition, Matrix4f projection, String name, String distance,
            int width, int height, List<LabelBounds> labels) {
        if (feet.w >= MINIMUM_W) {
            drawScreenLine(graphics, width / 2.0, height / 2.0,
                screenX(feet, width), screenY(feet, height), width, height, TRACER_COLOR);
        }

        // Each corner connects along X, Y and Z, yielding the twelve edges of the player's box.
        for (int corner = 0; corner < 8; corner++) {
            for (int axis = 1; axis <= 4; axis *= 2) {
                if ((corner & axis) != 0) {
                    continue;
                }
                Segment edge = clipProjectedLine(corners[corner], corners[corner | axis], width, height);
                if (edge != null) {
                    drawLine(graphics, edge, BOX_COLOR);
                }
            }
        }

        Vector4f top = project(new Vec3(
            bounds.getCenter().x, bounds.maxY, bounds.getCenter().z), cameraPosition, projection);
        Vector4f labelAnchor = top;
        if (labelAnchor.w < MINIMUM_W) {
            labelAnchor = feet;
        }
        if (labelAnchor.w < MINIMUM_W) {
            for (Vector4f corner : corners) {
                if (corner.w >= MINIMUM_W) {
                    labelAnchor = corner;
                    break;
                }
            }
        }
        if (labelAnchor.w >= MINIMUM_W) {
            drawLabel(graphics, name, distance, (int) Math.round(screenX(labelAnchor, width)),
                (int) Math.round(screenY(labelAnchor, height)) - (name.isEmpty() ? 13 : 23),
                width, height, labels);
        }
    }

    private static void drawOffscreenMarker(
            GuiGraphicsExtractor graphics, Vector4f feet, String name, String distance,
            int width, int height, List<LabelBounds> labels) {
        double centerX = width / 2.0;
        double centerY = height / 2.0;
        // Undivided clip coordinates preserve left/right and up/down for players behind the camera.
        double directionX = feet.x * width;
        double directionY = -feet.y * height;
        if (Math.abs(directionX) + Math.abs(directionY) < 0.0001) {
            directionY = height;
        }
        double scale = Math.min(
            (centerX - 14) / Math.max(0.0001, Math.abs(directionX)),
            (centerY - 14) / Math.max(0.0001, Math.abs(directionY)));
        double endX = centerX + directionX * scale;
        double endY = centerY + directionY * scale;
        drawScreenLine(graphics, centerX, centerY, endX, endY, width, height, TRACER_COLOR);
        int markerX = (int) Math.round(endX);
        int markerY = (int) Math.round(endY);
        graphics.fill(markerX - 1, markerY - 1, markerX + 2, markerY + 2, TRACER_COLOR);

        double length = Math.hypot(endX - centerX, endY - centerY);
        int labelX = (int) Math.round(endX - (endX - centerX) * 24.0 / length);
        int labelY = (int) Math.round(endY - (endY - centerY) * 24.0 / length)
            - (name.isEmpty() ? 5 : 10);
        drawLabel(graphics, name, distance, labelX, labelY, width, height, labels);
    }

    private static Vector4f project(Vec3 point, Vec3 camera, Matrix4f projection) {
        return projection.transform(new Vector4f(
            (float) (point.x - camera.x), (float) (point.y - camera.y),
            (float) (point.z - camera.z), 1.0F));
    }

    private static Vector4f[] projectCorners(AABB bounds, Vec3 camera, Matrix4f projection) {
        Vector4f[] corners = new Vector4f[8];
        for (int corner = 0; corner < 8; corner++) {
            corners[corner] = project(new Vec3(
                (corner & 1) == 0 ? bounds.minX : bounds.maxX,
                (corner & 2) == 0 ? bounds.minY : bounds.maxY,
                (corner & 4) == 0 ? bounds.minZ : bounds.maxZ), camera, projection);
        }
        return corners;
    }

    private static boolean isOnScreen(AABB bounds, Vec3 camera, Matrix4f projection) {
        return isOnScreen(projectCorners(bounds, camera, projection));
    }

    private static boolean isOnScreen(Vector4f[] corners) {
        List<Vector4f> frontPoints = new ArrayList<>(20);
        for (Vector4f corner : corners) {
            if (corner.w >= MINIMUM_W) {
                frontPoints.add(corner);
            }
        }
        for (int corner = 0; corner < 8; corner++) {
            for (int axis = 1; axis <= 4; axis *= 2) {
                if ((corner & axis) != 0) {
                    continue;
                }
                Vector4f first = corners[corner];
                Vector4f second = corners[corner | axis];
                if ((first.w >= MINIMUM_W) != (second.w >= MINIMUM_W)) {
                    float fraction = (MINIMUM_W - first.w) / (second.w - first.w);
                    frontPoints.add(new Vector4f(first).lerp(second, fraction));
                }
            }
        }
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (Vector4f point : frontPoints) {
            double x = point.x / point.w;
            double y = point.y / point.w;
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }
        return minX <= 1.0 && maxX >= -1.0 && minY <= 1.0 && maxY >= -1.0;
    }

    private static Segment clipProjectedLine(Vector4f first, Vector4f second, int width, int height) {
        double start = 0;
        double end = 1;
        double[] firstPlanes = {
            first.w - MINIMUM_W, first.x + first.w, first.w - first.x,
            first.y + first.w, first.w - first.y
        };
        double[] secondPlanes = {
            second.w - MINIMUM_W, second.x + second.w, second.w - second.x,
            second.y + second.w, second.w - second.y
        };
        for (int plane = 0; plane < firstPlanes.length; plane++) {
            double firstDistance = firstPlanes[plane];
            double secondDistance = secondPlanes[plane];
            if (firstDistance < 0 && secondDistance < 0) {
                return null;
            }
            if ((firstDistance < 0) != (secondDistance < 0)) {
                double fraction = firstDistance / (firstDistance - secondDistance);
                if (firstDistance < 0) {
                    start = Math.max(start, fraction);
                } else {
                    end = Math.min(end, fraction);
                }
            }
        }
        if (start > end) {
            return null;
        }
        Vector4f clippedFirst = new Vector4f(first).lerp(second, (float) start);
        Vector4f clippedSecond = new Vector4f(first).lerp(second, (float) end);
        return new Segment(screenX(clippedFirst, width), screenY(clippedFirst, height),
            screenX(clippedSecond, width), screenY(clippedSecond, height));
    }

    private static double screenX(Vector4f point, int width) {
        return (point.x / point.w * 0.5 + 0.5) * width;
    }

    private static double screenY(Vector4f point, int height) {
        return (0.5 - point.y / point.w * 0.5) * height;
    }

    private static void drawScreenLine(
            GuiGraphicsExtractor graphics, double firstX, double firstY, double secondX,
            double secondY, int width, int height, int color) {
        Vector4f first = new Vector4f(
            (float) (2.0 * firstX / width - 1), (float) (1 - 2.0 * firstY / height), 0, 1);
        Vector4f second = new Vector4f(
            (float) (2.0 * secondX / width - 1), (float) (1 - 2.0 * secondY / height), 0, 1);
        Segment clipped = clipProjectedLine(first, second, width, height);
        if (clipped != null) {
            drawLine(graphics, clipped, color);
        }
    }

    private static void drawLine(GuiGraphicsExtractor graphics, Segment line, int color) {
        double length = Math.hypot(line.secondX - line.firstX, line.secondY - line.firstY);
        if (length < 0.001) {
            return;
        }
        graphics.pose().pushMatrix();
        graphics.pose().translate((float) line.firstX, (float) line.firstY);
        graphics.pose().rotate((float) Math.atan2(
            line.secondY - line.firstY, line.secondX - line.firstX));
        graphics.fill(0, 0, Math.max(1, (int) Math.ceil(length)), 1, color);
        graphics.pose().popMatrix();
    }

    private static void drawLabel(
            GuiGraphicsExtractor graphics, String name, String distance, int centerX, int topY,
            int width, int height, List<LabelBounds> labels) {
        Minecraft game = Minecraft.getInstance();
        name = game.font.plainSubstrByWidth(name, width - 16);
        distance = game.font.plainSubstrByWidth(distance, width - 16);
        int textWidth = Math.max(game.font.width(name), game.font.width(distance));
        int labelHeight = name.isEmpty() ? 12 : 22;
        int spacing = labelHeight + 1;
        int left = Math.clamp(centerX - textWidth / 2, 5, width - textWidth - 5);
        int top = Math.clamp(topY, 4, height - labelHeight - 3);
        LabelBounds chosen = new LabelBounds(left - 2, top - 1, textWidth + 4, labelHeight);
        for (int offset = 0; offset < height / spacing; offset++) {
            int shift = ((offset + 1) / 2) * spacing * (offset % 2 == 0 ? 1 : -1);
            int candidateTop = Math.clamp(top + shift, 4, height - labelHeight - 3);
            LabelBounds candidate = new LabelBounds(
                left - 2, candidateTop - 1, textWidth + 4, labelHeight);
            boolean overlaps = false;
            for (LabelBounds previous : labels) {
                overlaps |= candidate.intersects(previous);
            }
            if (!overlaps) {
                chosen = candidate;
                break;
            }
        }
        labels.add(chosen);
        int textCenter = chosen.x + chosen.width / 2;
        graphics.fill(chosen.x, chosen.y, chosen.x + chosen.width,
            chosen.y + chosen.height, 0x75000000);
        if (!name.isEmpty()) {
            graphics.centeredText(game.font, name, textCenter, chosen.y + 2, NAME_COLOR);
        }
        graphics.centeredText(game.font, distance, textCenter,
            chosen.y + (name.isEmpty() ? 2 : 12), BOX_COLOR);
    }

    private record Segment(double firstX, double firstY, double secondX, double secondY) {
    }

    private record LabelBounds(int x, int y, int width, int height) {
        boolean intersects(LabelBounds other) {
            return x < other.x + other.width && x + width > other.x
                && y < other.y + other.height && y + height > other.y;
        }
    }
}
