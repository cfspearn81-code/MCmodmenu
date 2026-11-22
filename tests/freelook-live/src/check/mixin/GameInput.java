package check.mixin;

import check.FreeLookCheck;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class GameInput {
    @Inject(method = "tick", at = @At("HEAD"))
    private void check$tick(CallbackInfo callback) {
        FreeLookCheck.tick();
    }

    @Inject(method = "isWindowActive", at = @At("RETURN"), cancellable = true)
    private void check$focus(CallbackInfoReturnable<Boolean> callback) {
        callback.setReturnValue(FreeLookCheck.focus);
    }

    @Mixin(InputConstants.class)
    public static abstract class Input {
        @Inject(method = "isKeyDown", at = @At("HEAD"), cancellable = true)
        private static void check$key(int key, CallbackInfoReturnable<Boolean> callback) {
            if (key == InputConstants.KEY_LALT || key == InputConstants.KEY_F7) {
                callback.setReturnValue(key == FreeLookCheck.heldKey);
            } else if (key == InputConstants.KEY_R) {
                callback.setReturnValue(FreeLookCheck.aimHold);
            }
        }
    }
}
