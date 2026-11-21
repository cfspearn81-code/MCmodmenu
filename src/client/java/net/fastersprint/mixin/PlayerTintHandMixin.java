package net.fastersprint.mixin;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fastersprint.PlayerTint;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(AvatarRenderer.class)
abstract class PlayerTintHandMixin {
    @Inject(method="renderHand",at=@At("TAIL"))
    private void fastersprint$handEdges(PoseStack pose,SubmitNodeCollector out,int light,Identifier skin,ModelPart part,boolean sleeve,CallbackInfo ci){PlayerTint.hand(part,pose,out);}
}
