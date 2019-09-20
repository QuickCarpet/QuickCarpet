package quickcarpet.utils;

import net.minecraft.block.BlockRenderLayer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.BufferBuilder;

public interface IBlockEntityRenderDispatcher {
    void renderBlockEntityOffset(BlockEntity blockEntity, float partialTicks, int destroyStage, BlockRenderLayer renderLayer, BufferBuilder bufferBuilder, double xOffset, double yOffset, double zOffset);
}
