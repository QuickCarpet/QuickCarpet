package quickcarpet.utils.extensions;

import net.minecraft.block.BlockEntityProvider;

public interface DynamicBlockEntityProvider extends BlockEntityProvider {
    boolean quickcarpet$providesBlockEntity();
}
