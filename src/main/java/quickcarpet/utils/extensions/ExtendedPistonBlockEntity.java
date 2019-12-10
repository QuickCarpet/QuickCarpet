package quickcarpet.utils.extensions;

import net.minecraft.block.entity.BlockEntity;

public interface ExtendedPistonBlockEntity {
    void setCarriedBlockEntity(BlockEntity blockEntity);
    BlockEntity getCarriedBlockEntity();
    void setRenderCarriedBlockEntity(boolean render);
    boolean getRenderCarriedBlockEntity();
    boolean isRenderModeSet();
}
