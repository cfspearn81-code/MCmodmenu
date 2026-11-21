package live.mixin;
import live.LiveTest;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(MultiPlayerGameMode.class)
public abstract class InteractionMixin {
 @Inject(method="useItemOn",at=@At("HEAD"))
 private void live$count(CallbackInfoReturnable<net.minecraft.world.InteractionResult> ci){LiveTest.interaction();}
 @Inject(method="useItemOn",at=@At("RETURN"))
 private void live$predicted(net.minecraft.client.player.LocalPlayer player, net.minecraft.world.InteractionHand hand,
      net.minecraft.world.phys.BlockHitResult hit, CallbackInfoReturnable<net.minecraft.world.InteractionResult> ci){
  var level=net.minecraft.client.Minecraft.getInstance().level;
  if(LiveTest.running && LiveTest.scenario.equals("reject") && !level.getBlockState(hit.getBlockPos().relative(hit.getDirection())).canBeReplaced())
   System.out.println("GHOST TEST: client predicted solid placement before server reply");
 }
}
