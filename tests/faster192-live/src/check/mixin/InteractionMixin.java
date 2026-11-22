package check.mixin;
import check.GhostCheck;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(MultiPlayerGameMode.class)
public abstract class InteractionMixin {
 @Inject(method="useItemOn",at=@At("RETURN"))private void attempt(LocalPlayer player,InteractionHand hand,BlockHitResult hit,CallbackInfoReturnable<InteractionResult> ci){GhostCheck.interaction(hit);}
}
