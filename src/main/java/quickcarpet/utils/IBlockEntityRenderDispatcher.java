package quickcarpet.utils;

import net.minecraft.block.entity.BlockEntity;

public interface IBlockEntityRenderDispatcher
{
    void renderBlockEntityOffset(BlockEntity blockEntity_1, float partialTicks, int destroyStage, double xOffset, double yOffset, double zOffset);
}
