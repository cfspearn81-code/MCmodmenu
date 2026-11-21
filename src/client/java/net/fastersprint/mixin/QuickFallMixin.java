package net.fastersprint.mixin;
import net.fastersprint.QuickFall;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LocalPlayer.class)
abstract class QuickFallMixin {
    @Inject(method="aiStep",at=@At("RETURN"))
    private void fastersprint$accelerateWalkingDrop(CallbackInfo callback){QuickFall.afterMovement((LocalPlayer)(Object)this);}
}
