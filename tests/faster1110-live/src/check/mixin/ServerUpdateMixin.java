package check.mixin;
import check.GhostCheck;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ClientLevel.class)
public abstract class ServerUpdateMixin {
 @Inject(method="setServerVerifiedBlockState",at=@At("HEAD"),cancellable=true)private void update(BlockPos pos,BlockState state,int flags,CallbackInfo ci){if(GhostCheck.serverUpdate((ClientLevel)(Object)this,pos,state,flags))ci.cancel();}
}
