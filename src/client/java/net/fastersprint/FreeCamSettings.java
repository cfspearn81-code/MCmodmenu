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

/** Only the binding persists. A new session always starts with the normal camera. */
public final class FreeCamSettings {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("faster-freecam.properties");
    private static boolean loaded;
    private static int key = InputConstants.KEY_F6;
    private FreeCamSettings() { }
    public static int key() {
        if (!loaded) {
            loaded = true;
            if (Files.exists(FILE)) try (Reader reader = Files.newBufferedReader(FILE)) {
                Properties properties = new Properties(); properties.load(reader);
                int parsed = Integer.parseInt(properties.getProperty("key", Integer.toString(key)));
                if (valid(parsed)) key = parsed;
            } catch (IOException | IllegalArgumentException failure) {
                LoggerFactory.getLogger("fastersprint").warn("Could not read Free Cam key", failure);
            }
        }
        return key;
    }
    public static boolean valid(int value) {
        return value >= InputConstants.KEY_A && value < 512
            && value != InputConstants.KEY_P && value != InputConstants.KEY_ESCAPE;
    }
    public static void setKey(int value) throws IOException {
        if (!valid(value)) throw new IllegalArgumentException("Choose a key other than P or Escape");
        key(); Files.createDirectories(FILE.getParent());
        Properties properties = new Properties(); properties.setProperty("key", Integer.toString(value));
        Path temporary = Files.createTempFile(FILE.getParent(), "faster-freecam-", ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(temporary)) { properties.store(writer, "Faster Free Cam (SDL scancode)"); }
            Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING); key = value;
        } finally { Files.deleteIfExists(temporary); }
    }
}
