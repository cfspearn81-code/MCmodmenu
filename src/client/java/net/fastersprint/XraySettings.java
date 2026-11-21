package net.fastersprint;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.EnumSet;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.LoggerFactory;

public final class XraySettings {
    public static final int MIN_RANGE = 1;
    public static final int MAX_RANGE = 5000;
    public static final int DEFAULT_RANGE = 200;
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("faster-movement-xray.properties");
    private static boolean loaded;
    private static boolean enabled;
    private static int range = DEFAULT_RANGE;
    private static int oreMask = (1 << OreType.values().length) - 1;
    private static EnumSet<OreType> ores = EnumSet.allOf(OreType.class);

    private XraySettings() { }
    public static boolean enabled() { load(); return enabled; }
    public static int range() { load(); return range; }
    public static int mask() { load(); return oreMask; }
    public static boolean oreEnabled(OreType ore) { load(); return ores.contains(ore); }
    public static void setEnabled(boolean next) throws IOException { load(); save(next, range, ores); }
    public static void setRange(int next) throws IOException {
        if (next < MIN_RANGE || next > MAX_RANGE) throw new IllegalArgumentException("Range must be 1–5000 blocks");
        load(); save(enabled, next, ores);
    }
    public static void setOreEnabled(OreType ore, boolean next) throws IOException {
        load();
        EnumSet<OreType> copy = ores.clone();
        if (next) copy.add(ore); else copy.remove(ore);
        save(enabled, range, copy);
    }
    private static void load() {
        if (loaded) return;
        loaded = true;
        if (!Files.exists(FILE)) return;
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(FILE)) {
            properties.load(reader);
            enabled = Boolean.parseBoolean(properties.getProperty("enabled", "false"));
            try {
                int parsed = Integer.parseInt(properties.getProperty("range", Integer.toString(DEFAULT_RANGE)));
                if (parsed >= MIN_RANGE && parsed <= MAX_RANGE) range = parsed;
            } catch (NumberFormatException ignored) { }
            for (OreType ore : OreType.values()) {
                if (!Boolean.parseBoolean(properties.getProperty("ore." + ore.name().toLowerCase(java.util.Locale.ROOT), "true"))) ores.remove(ore);
            }
            updateMask();
        } catch (IOException | IllegalArgumentException failure) {
            LoggerFactory.getLogger("fastersprint").warn("Could not read Xray settings", failure);
        }
    }
    private static void updateMask() { oreMask=0; for (OreType ore : ores) oreMask |= 1 << ore.ordinal(); }
    private static void save(boolean nextEnabled, int nextRange, EnumSet<OreType> nextOres) throws IOException {
        Files.createDirectories(FILE.getParent());
        Properties properties = new Properties();
        properties.setProperty("enabled", Boolean.toString(nextEnabled));
        properties.setProperty("range", Integer.toString(nextRange));
        for (OreType ore : OreType.values()) properties.setProperty("ore." + ore.name().toLowerCase(java.util.Locale.ROOT), Boolean.toString(nextOres.contains(ore)));
        Path temporary = Files.createTempFile(FILE.getParent(), "faster-xray-", ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(temporary)) { properties.store(writer, "Faster Movement Xray"); }
            Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING);
            enabled = nextEnabled; range = nextRange; ores = nextOres.clone(); updateMask();
        } finally { Files.deleteIfExists(temporary); }
    }
}
