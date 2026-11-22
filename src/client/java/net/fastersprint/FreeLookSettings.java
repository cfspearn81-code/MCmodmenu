package net.fastersprint;

import com.mojang.blaze3d.platform.InputConstants;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.LoggerFactory;

public final class FreeLookSettings {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("faster-freelook.properties");
    private static boolean loaded;
    private static boolean enabled = true;
    private static int key = InputConstants.KEY_LALT;

    private FreeLookSettings() { }

    public static boolean enabled() {
        load();
        return enabled;
    }

    public static int key() {
        load();
        return key;
    }

    public static boolean valid(int value) {
        return value >= InputConstants.KEY_A && value < 512
            && value != InputConstants.KEY_P && value != InputConstants.KEY_ESCAPE;
    }

    public static void setEnabled(boolean value) throws IOException {
        load();
        save(value, key);
    }

    public static void setKey(int value) throws IOException {
        if (!valid(value)) throw new IllegalArgumentException("Choose a key other than P or Escape");
        load();
        save(enabled, value);
    }

    private static void load() {
        if (loaded) return;
        loaded = true;
        if (!Files.exists(FILE)) return;
        try (Reader reader = Files.newBufferedReader(FILE)) {
            Properties properties = new Properties();
            properties.load(reader);
            enabled = !"false".equalsIgnoreCase(properties.getProperty("enabled"));
            int parsed = Integer.parseInt(properties.getProperty("key", Integer.toString(key)));
            if (valid(parsed)) key = parsed;
        } catch (IOException | IllegalArgumentException failure) {
            LoggerFactory.getLogger("fastersprint").warn("Could not read Free Look settings", failure);
        }
    }

    private static void save(boolean nextEnabled, int nextKey) throws IOException {
        Files.createDirectories(FILE.getParent());
        Properties properties = new Properties();
        properties.setProperty("enabled", Boolean.toString(nextEnabled));
        properties.setProperty("key", Integer.toString(nextKey));
        Path temporary = Files.createTempFile(FILE.getParent(), "faster-freelook-", ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(temporary)) {
                properties.store(writer, "Faster Free Look (hold key, SDL scancode)");
            }
            Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING);
            enabled = nextEnabled;
            key = nextKey;
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
