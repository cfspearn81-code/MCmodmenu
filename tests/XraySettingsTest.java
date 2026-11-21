import net.fastersprint.OreType;
import net.fastersprint.XraySettings;

public final class XraySettingsTest {
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        if (args[0].equals("reload")) {
            check(XraySettings.enabled(), "enabled did not persist");
            check(XraySettings.range() == 5000, "maximum did not persist");
            for (OreType ore : OreType.values()) check(XraySettings.oreEnabled(ore) == (ore.ordinal() % 2 == 0), "ore did not persist: " + ore);
        } else if (args[0].equals("invalid")) {
            check(XraySettings.range() == 200, "invalid file did not use default");
        } else {
            check(!XraySettings.enabled(), "default must be off");
            check(XraySettings.range() == 200, "default must be 200");
            for (OreType ore : OreType.values()) check(XraySettings.oreEnabled(ore), "default ore disabled");
            XraySettings.setRange(1); check(XraySettings.range() == 1, "minimum rejected");
            XraySettings.setRange(5000); check(XraySettings.range() == 5000, "maximum rejected");
            for (int invalid : new int[]{-1, 0, 5001, Integer.MAX_VALUE}) {
                try { XraySettings.setRange(invalid); throw new AssertionError("accepted " + invalid); }
                catch (IllegalArgumentException expected) { }
                check(XraySettings.range() == 5000, "invalid changed range");
            }
            for (OreType ore : OreType.values()) XraySettings.setOreEnabled(ore, ore.ordinal() % 2 == 0);
            XraySettings.setEnabled(true);
            check(XraySettings.range() == 5000, "toggle lost range");
        }
        System.out.println("PASS Xray settings " + args[0]);
    }
}
