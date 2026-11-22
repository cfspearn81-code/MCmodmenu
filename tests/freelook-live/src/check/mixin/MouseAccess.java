package check.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MouseHandler.class)
public interface MouseAccess {
    @Accessor("accumulatedDX")
    void check$setX(double x);

    @Accessor("accumulatedDY")
    void check$setY(double y);

    @Invoker("turnPlayer")
    void check$turn(double seconds);
}
