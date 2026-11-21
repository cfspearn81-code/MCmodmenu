import net.fastersprint.NametagColor;
import net.minecraft.network.chat.Component;
public final class NametagColorTest {
    public static void main(String[] args) {
        check(Component.literal("Alex"),0xFFFFFF,0xFFFFFFFF,"plain white");
        check(Component.literal("Alex").withColor(0xFF5555),0xFFFFFF,0xFFFF5555,"red name");
        check(Component.literal("Alex").withColor(0x5555FF),0xFFFFFF,0xFF5555FF,"blue name");
        check(Component.literal("Alex"),0xFF5555,0xFFFF5555,"team fallback");
        check(Component.literal("[Rank] ").withColor(0x55FF55)
            .append(Component.literal("Alex").withColor(0x5555FF)),0xFFFFFF,0xFF5555FF,"rank color does not override name");
        check(Component.empty().withColor(0xFF5555).append(Component.literal("[Rank] ").withColor(0x55FF55))
            .append(Component.literal("Alex")),0xFFFFFF,0xFFFF5555,"inherited name color");
        check(Component.literal("Alex").withColor(0x123456),0xFFFFFF,0xFF123456,"custom RGB and opaque alpha");
        var changing=Component.literal("Alex").withColor(0xFF5555);
        check(changing,0xFFFFFF,0xFFFF5555,"initial color");
        changing.withColor(0x5555FF);
        check(changing,0xFFFFFF,0xFF5555FF,"updated team/name color without cached old value");
        System.out.println("PASS: 9 actual Minecraft component color checks");
    }
    private static void check(Component name,int fallback,int expected,String message) {
        int actual=NametagColor.resolve(name,"Alex",fallback);
        if(actual!=expected) throw new AssertionError(message+": "+Integer.toHexString(actual));
    }
}
