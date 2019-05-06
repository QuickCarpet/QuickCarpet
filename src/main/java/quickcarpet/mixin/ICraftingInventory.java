package quickcarpet.mixin;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CraftingInventory.class)
public interface ICraftingInventory {
    @Accessor("stacks")
    void setInventory(DefaultedList<ItemStack> inventory);
}
