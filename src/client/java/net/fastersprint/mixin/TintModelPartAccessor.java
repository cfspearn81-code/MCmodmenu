package net.fastersprint.mixin;
import java.util.*;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(ModelPart.class)
public interface TintModelPartAccessor {
    @Accessor("cubes") List<ModelPart.Cube> fastersprint$cubes();
    @Accessor("children") Map<String,ModelPart> fastersprint$children();
}
