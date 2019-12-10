package quickcarpet.mixin.accessor;

import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemDispenserBehavior.class)
public interface ItemDispenserBehaviorAccessor {
    @Invoker("dispenseSilently")
    ItemStack doDispenseSilently(BlockPointer pointer, ItemStack stack);
}
