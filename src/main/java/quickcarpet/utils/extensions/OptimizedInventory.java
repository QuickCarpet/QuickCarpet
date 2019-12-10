package quickcarpet.utils.extensions;

import net.minecraft.inventory.Inventory;
import quickcarpet.utils.InventoryOptimizer;

import javax.annotation.Nullable;

public interface OptimizedInventory extends Inventory {
    @Nullable
    InventoryOptimizer getOptimizer();
}
