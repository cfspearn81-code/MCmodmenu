package net.fastersprint.mixin;

import net.fastersprint.AutoBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
abstract class AutoBridgeBoundaryMixin {
    @ModifyVariable(method = "move", at = @At("STORE"), ordinal = 1)
    private Vec3 fastersprint$limitBridgeStep(Vec3 resolved, MoverType type, Vec3 requested) {
        if (type == MoverType.SELF && (Object) this == Minecraft.getInstance().player) {
            // Collision resolution can change a diagonal step before the position is applied.
            return AutoBridge.guardMovement(resolved);
        }
        return resolved;
    }
}
