package net.fastersprint.mixin;
import net.minecraft.client.renderer.rendertype.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(RenderType.class)
public interface TintRenderTypeInvoker {
    @Invoker("create") static RenderType fastersprint$create(String name,RenderSetup setup){throw new AssertionError();}
}
