package net.fastersprint.mixin;

import net.fastersprint.Xray;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
abstract class XrayChunkMixin {
    @Inject(method = "onChunkLoaded", at = @At("TAIL"))
    private void fastersprint$trackOres(ChunkPos pos, CallbackInfo callback) {
        Xray.chunkLoaded((ClientLevel) (Object) this, pos);
    }
    @Inject(method = "setBlocksDirty", at = @At("TAIL"))
    private void fastersprint$updateOre(BlockPos pos, BlockState previous, BlockState next, CallbackInfo callback) {
        Xray.blockChanged((ClientLevel) (Object) this, pos, next);
    }
    @Inject(method = "unload", at = @At("HEAD"))
    private void fastersprint$forgetOres(LevelChunk chunk, CallbackInfo callback) {
        Xray.chunkUnloaded((ClientLevel) (Object) this, chunk.getPos());
    }
}
