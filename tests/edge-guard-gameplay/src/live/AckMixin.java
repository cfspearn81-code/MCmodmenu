package live.mixin;
import live.LiveTest;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(ClientLevel.class)
public abstract class AckMixin {
 @Inject(method="handleBlockChangedAck",at=@At("HEAD"),cancellable=true)
 private void live$delay(int sequence,CallbackInfo ci){if(LiveTest.delay((ClientLevel)(Object)this,sequence))ci.cancel();}
}
