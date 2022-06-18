package quickcarpet.utils.mixin.extensions;

import net.minecraft.block.entity.BlockEntity;

public interface ExtendedPistonBlockEntity {
    void quickcarpet$setCarriedBlockEntity(BlockEntity blockEntity);
    BlockEntity quickcarpet$getCarriedBlockEntity();
    void quickcarpet$setRenderCarriedBlockEntity(boolean render);
    boolean quickcarpet$getRenderCarriedBlockEntity();
    boolean quickcarpet$isRenderModeSet();
}
