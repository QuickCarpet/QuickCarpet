package quickcarpet.mixin.fillUpdates.compat.worldedit;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.utils.extensions.ExtendedWorldChunkFillUpdates;

import javax.annotation.Nullable;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin implements ExtendedWorldChunkFillUpdates {
    @Shadow private volatile boolean shouldSave;
    @Shadow @Nullable public abstract BlockState setBlockState(BlockPos pos, BlockState state, boolean moved);
    @Shadow @Nullable public abstract BlockEntity getBlockEntity(BlockPos pos, WorldChunk.CreationType creationType);
    @Shadow public abstract void addBlockEntity(BlockEntity blockEntity);
    @Shadow protected abstract <T extends BlockEntity> void updateTicker(T blockEntity);

    private boolean fillUpdates = true;

    @Override
    @Nullable
    public BlockState setBlockStateWithoutUpdates(BlockPos pos, BlockState state, boolean moved) {
        try {
            fillUpdates = false;
            return setBlockState(pos, state, moved);
        } finally {
            fillUpdates = true;
        }
    }

    // World Edit is redirecting onAdded()
    // Litematica is redirecting isClient
    // so this is the only way left to cancel the onAdded() call
    @Inject(method = "setBlockState", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;isClient:Z", ordinal = 1), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void cancelFillUpdates(BlockPos pos, BlockState state, boolean moved, CallbackInfoReturnable<BlockState> cir, int posY, int sectionY, ChunkSection section, boolean empty, int inSectionX, int inSectionY, int inSectionZ, BlockState blockState) {
        if (fillUpdates) return;
        Block block = state.getBlock();
        if (state.hasBlockEntity()) {
            BlockEntity blockEntity = this.getBlockEntity(pos, WorldChunk.CreationType.CHECK);
            if (blockEntity == null) {
                blockEntity = ((BlockEntityProvider)block).createBlockEntity(pos, state);
                if (blockEntity != null) {
                    this.addBlockEntity(blockEntity);
                }
            } else {
                blockEntity.setCachedState(state);
                this.updateTicker(blockEntity);
            }
        }

        this.shouldSave = true;
        cir.setReturnValue(blockState);
    }

    @Redirect(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onStateReplaced(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)V"))
    private void onReplaced(BlockState oldState, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (fillUpdates) {
            oldState.onStateReplaced(world, pos, newState, moved);
        } else {
            if (oldState.hasBlockEntity() && !oldState.isOf(newState.getBlock())) {
                world.removeBlockEntity(pos);
            }
        }
    }
}
