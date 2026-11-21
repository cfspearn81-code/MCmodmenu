package net.fastersprint;

import java.io.IOException;
import com.mojang.blaze3d.platform.InputConstants;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PlayerDisplaySettings {
    private static final Logger LOGGER = LoggerFactory.getLogger("fastersprint");
    private static final Path FILE = FabricLoader.getInstance().getConfigDir()
        .resolve("faster-movement.properties");
    private static final Values DEFAULTS = new Values(true, 500, 10, 1.5, false, false, InputConstants.KEY_B, false);
    private static Values values = DEFAULTS;
    private static boolean loaded;

    private PlayerDisplaySettings() {
    }

    public static boolean enabled() {
        return current().playerDisplayEnabled;
    }

    public static int range() {
        return current().range;
    }

    public static int nameLimit() {
        return current().nameLimit;
    }

    public static double speedMultiplier() {
        return current().speedMultiplier;
    }

    public static boolean guiMoveEnabled() {
        return current().guiMoveEnabled;
    }

    public static void setEnabled(boolean enabled) throws IOException {
        Values previous = current();
        save(new Values(enabled, previous.range, previous.nameLimit,
            previous.speedMultiplier, previous.guiMoveEnabled, previous.autoBridgeEnabled, previous.autoBridgeKey, previous.autoHitEnabled));
    }

    public static void setRange(int range) throws IOException {
        if (range < 1) {
            throw new IllegalArgumentException("Range must be a positive whole number");
        }
        Values previous = current();
        save(new Values(previous.playerDisplayEnabled, range, previous.nameLimit,
            previous.speedMultiplier, previous.guiMoveEnabled, previous.autoBridgeEnabled, previous.autoBridgeKey, previous.autoHitEnabled));
    }

    public static void setNameLimit(int nameLimit) throws IOException {
        if (nameLimit < 0) {
            throw new IllegalArgumentException("Name limit must be zero or more");
        }
        Values previous = current();
        save(new Values(previous.playerDisplayEnabled, previous.range, nameLimit,
            previous.speedMultiplier, previous.guiMoveEnabled, previous.autoBridgeEnabled, previous.autoBridgeKey, previous.autoHitEnabled));
    }

    public static void setSpeedMultiplier(double multiplier) throws IOException {
        if (!Double.isFinite(multiplier) || multiplier < 1.0) {
            throw new IllegalArgumentException("Speed multiplier must be at least 1");
        }
        Values previous = current();
        save(new Values(previous.playerDisplayEnabled, previous.range, previous.nameLimit,
            multiplier, previous.guiMoveEnabled, previous.autoBridgeEnabled, previous.autoBridgeKey, previous.autoHitEnabled));
    }

    public static void setGuiMoveEnabled(boolean enabled) throws IOException {
        Values previous = current();
        save(new Values(previous.playerDisplayEnabled, previous.range, previous.nameLimit,
            previous.speedMultiplier, enabled, previous.autoBridgeEnabled, previous.autoBridgeKey, previous.autoHitEnabled));
    }

    public static boolean autoBridgeEnabled() { return current().autoBridgeEnabled; }

    public static int autoBridgeKey() { return current().autoBridgeKey; }

    public static void setAutoBridgeEnabled(boolean enabled) throws IOException {
        Values v = current();
        save(new Values(v.playerDisplayEnabled, v.range, v.nameLimit, v.speedMultiplier,
            v.guiMoveEnabled, enabled, v.autoBridgeKey, v.autoHitEnabled));
    }

    public static void setAutoBridgeKey(int key) throws IOException {
        if (!validBridgeKey(key)) {
            throw new IllegalArgumentException("Choose a keyboard key other than P or Escape");
        }
        Values v = current();
        save(new Values(v.playerDisplayEnabled, v.range, v.nameLimit, v.speedMultiplier,
            v.guiMoveEnabled, v.autoBridgeEnabled, key, v.autoHitEnabled));
    }

    public static boolean autoHitEnabled() { return current().autoHitEnabled; }

    public static void setAutoHitEnabled(boolean enabled) throws IOException {
        Values v = current();
        save(new Values(v.playerDisplayEnabled, v.range, v.nameLimit, v.speedMultiplier,
            v.guiMoveEnabled, v.autoBridgeEnabled, v.autoBridgeKey, enabled));
    }

    private static boolean validBridgeKey(int key) {
        // 26.3 uses SDL scancodes, not GLFW/ASCII key codes. Letters occupy 4–29.
        return key >= InputConstants.KEY_A && key < 512
            && key != InputConstants.KEY_P && key != InputConstants.KEY_ESCAPE;
    }

    private static int readBridgeKey(Properties properties) {
        try {
            int key = Integer.parseInt(properties.getProperty("autoBridge.key",
                Integer.toString(InputConstants.KEY_B)));
            // The first Auto Bridge build accidentally used GLFW's B (66) as its default.
            if (!properties.containsKey("autoBridge.keyFormat") && key == 66) {
                return InputConstants.KEY_B;
            }
            return validBridgeKey(key) ? key : InputConstants.KEY_B;
        } catch (NumberFormatException invalid) {
            return InputConstants.KEY_B;
        }
    }

    private static Values current() {
        if (loaded) {
            return values;
        }
        loaded = true;
        if (!Files.exists(FILE)) {
            return values;
        }
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(FILE)) {
            properties.load(reader);
            values = new Values(
                Boolean.parseBoolean(properties.getProperty("players.enabled", "true")),
                readInteger(properties, "players.range", 500, 1),
                readInteger(properties, "players.nameLimit", 10, 0),
                readMultiplier(properties),
                Boolean.parseBoolean(properties.getProperty("guiMove.enabled",
                    properties.getProperty("craftingMovement.enabled", "false"))),
                Boolean.parseBoolean(properties.getProperty("autoBridge.enabled", "false")),
                readBridgeKey(properties),
                Boolean.parseBoolean(properties.getProperty("autoHit.enabled", "false")));
        } catch (IOException | IllegalArgumentException failure) {
            LOGGER.warn("Could not read Faster Movement settings", failure);
        }
        return values;
    }

    private static int readInteger(Properties properties, String key, int fallback, int minimum) {
        try {
            return Math.max(minimum, Integer.parseInt(properties.getProperty(key, Integer.toString(fallback))));
        } catch (NumberFormatException invalid) {
            return fallback;
        }
    }

    private static double readMultiplier(Properties properties) {
        try {
            double multiplier = Double.parseDouble(properties.getProperty("speed.multiplier", "1.5"));
            return Double.isFinite(multiplier) && multiplier >= 1.0 ? multiplier : 1.5;
        } catch (NumberFormatException invalid) {
            return 1.5;
        }
    }

    private static void save(Values next) throws IOException {
        Files.createDirectories(FILE.getParent());
        Properties properties = new Properties();
        properties.setProperty("players.enabled", Boolean.toString(next.playerDisplayEnabled));
        properties.setProperty("players.range", Integer.toString(next.range));
        properties.setProperty("players.nameLimit", Integer.toString(next.nameLimit));
        properties.setProperty("speed.multiplier", Double.toString(next.speedMultiplier));
        properties.setProperty("guiMove.enabled", Boolean.toString(next.guiMoveEnabled));
        properties.setProperty("autoBridge.enabled", Boolean.toString(next.autoBridgeEnabled));
        properties.setProperty("autoBridge.keyFormat", "sdl-scancode-v1");
        properties.setProperty("autoBridge.key", Integer.toString(next.autoBridgeKey));
        properties.setProperty("autoHit.enabled", Boolean.toString(next.autoHitEnabled));
        Path temporary = Files.createTempFile(FILE.getParent(), "faster-movement-", ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(temporary)) {
                properties.store(writer, "Faster Movement");
            }
            Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING);
            values = next;
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private record Values(boolean playerDisplayEnabled, int range, int nameLimit,
            double speedMultiplier, boolean guiMoveEnabled, boolean autoBridgeEnabled, int autoBridgeKey, boolean autoHitEnabled) {
    }
}
