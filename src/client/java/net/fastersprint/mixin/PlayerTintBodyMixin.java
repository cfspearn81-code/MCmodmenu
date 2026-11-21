package net.fastersprint.mixin;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fastersprint.PlayerTint;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.*;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LivingEntityRenderer.class)
abstract class PlayerTintBodyMixin {
    @Shadow protected EntityModel model;
    @Inject(method="submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V",ordinal=0))
    private void fastersprint$bodyEdges(LivingEntityRenderState state,PoseStack pose,SubmitNodeCollector out,CameraRenderState camera,CallbackInfo ci){
        if(state instanceof AvatarRenderState avatar&&PlayerTint.body(avatar))PlayerTint.submit(model.root(),pose,out);
    }
}
