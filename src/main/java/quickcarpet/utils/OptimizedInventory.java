package quickcarpet.utils;

import net.minecraft.inventory.Inventory;

import javax.annotation.Nullable;

public interface OptimizedInventory extends Inventory {
    @Nullable
    InventoryOptimizer getOptimizer();
}
