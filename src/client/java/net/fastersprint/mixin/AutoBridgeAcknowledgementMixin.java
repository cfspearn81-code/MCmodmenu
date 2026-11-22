package net.fastersprint.mixin;

import net.fastersprint.AutoBridge;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
abstract class AutoBridgeAcknowledgementMixin {
    @Inject(method = "handleBlockChangedAck", at = @At("TAIL"))
    private void fastersprint$confirmPlacement(int sequence, CallbackInfo callback) {
        AutoBridge.acknowledge((ClientLevel) (Object) this, sequence);
    }
    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At("RETURN"))
    private void fastersprint$quarantinePrediction(BlockPos pos, BlockState state, int flags, int depth, CallbackInfoReturnable<Boolean> callback) {
        ClientLevel level = (ClientLevel) (Object) this;
        if (callback.getReturnValueZ() && ((ClientLevelPredictionAccessor) level).fastersprint$predictionHandler().isPredicting()
                && !state.getCollisionShape(level, pos).isEmpty()) AutoBridge.predictedBlock(level, pos);
    }

    @Inject(method = "setServerVerifiedBlockState", at = @At("TAIL"))
    private void fastersprint$observeServerBlock(BlockPos pos, BlockState state, int flags, CallbackInfo callback) {
        AutoBridge.serverBlock((ClientLevel) (Object) this, pos, state);
    }

    @Inject(method = "unload", at = @At("TAIL"))
    private void fastersprint$forgetUnloadedChunk(LevelChunk chunk, CallbackInfo callback) {
        AutoBridge.unload((ClientLevel) (Object) this, chunk.getPos());
    }
}
