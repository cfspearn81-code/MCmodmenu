package net.fastersprint;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.LoggerFactory;

public final class QuickFallSettings {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("faster-quickfall.properties");
    private static boolean loaded, enabled;
    private QuickFallSettings() { }
    public static boolean enabled() {
        if (!loaded) {
            loaded = true;
            if (Files.exists(FILE)) try (Reader reader = Files.newBufferedReader(FILE)) {
                Properties properties = new Properties(); properties.load(reader);
                enabled = Boolean.parseBoolean(properties.getProperty("enabled", "false"));
            } catch (IOException | IllegalArgumentException failure) {
                LoggerFactory.getLogger("fastersprint").warn("Could not read QuickFall settings", failure);
            }
        }
        return enabled;
    }
    public static void setEnabled(boolean next) throws IOException {
        enabled(); Files.createDirectories(FILE.getParent());
        Properties properties = new Properties(); properties.setProperty("enabled", Boolean.toString(next));
        Path temporary = Files.createTempFile(FILE.getParent(), "faster-quickfall-", ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(temporary)) { properties.store(writer, "Faster QuickFall"); }
            Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING); enabled = next;
        } finally { Files.deleteIfExists(temporary); }
    }
}
