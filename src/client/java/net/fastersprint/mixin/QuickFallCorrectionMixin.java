package net.fastersprint.mixin;
import net.fastersprint.QuickFall;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ClientPacketListener.class)
abstract class QuickFallCorrectionMixin {
    @Inject(method="handleMovePlayer",at=@At("RETURN"))
    private void fastersprint$respectAuthoritativeMove(CallbackInfo callback){QuickFall.reset();}
}
