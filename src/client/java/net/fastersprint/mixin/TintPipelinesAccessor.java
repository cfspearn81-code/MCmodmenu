package net.fastersprint.mixin;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.*;
@Mixin(RenderPipelines.class)
public interface TintPipelinesAccessor {
    @Accessor("LINES_SNIPPET") static RenderPipeline.Snippet fastersprint$lines(){throw new AssertionError();}
    @Invoker("register") static RenderPipeline fastersprint$register(RenderPipeline pipeline){throw new AssertionError();}
}
