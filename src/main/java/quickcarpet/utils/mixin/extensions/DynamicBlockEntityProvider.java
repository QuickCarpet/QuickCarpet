package quickcarpet.utils.mixin.extensions;

import net.minecraft.block.BlockEntityProvider;

public interface DynamicBlockEntityProvider extends BlockEntityProvider {
    boolean quickcarpet$providesBlockEntity();
}
