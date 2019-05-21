package quickcarpet.utils;

import net.minecraft.block.entity.BlockEntity;

public interface IPistonBlockEntity
{
    void setCarriedBlockEntity(BlockEntity blockEntity);
    BlockEntity getCarriedBlockEntity();
    void setRenderCarriedBlockEntity(boolean b);
    boolean getRenderCarriedBlockEntity();
    boolean isRenderModeSet();
}
