package quickcarpet.mixin.accessor;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import quickcarpet.annotation.Feature;

@Feature("autoCraftingTable")
@Mixin(CraftingInventory.class)
public interface CraftingInventoryAccessor {
    @Accessor("stacks")
    void setInventory(DefaultedList<ItemStack> inventory);
}
