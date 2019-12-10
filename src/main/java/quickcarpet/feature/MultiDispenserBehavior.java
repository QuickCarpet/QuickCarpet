package quickcarpet.feature;

import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointer;
import quickcarpet.mixin.accessor.FallibleItemDispenserBehaviorAccessor;
import quickcarpet.mixin.accessor.ItemDispenserBehaviorAccessor;

public class MultiDispenserBehavior extends FallibleItemDispenserBehavior {
    private ItemDispenserBehavior[] behaviors;

    public MultiDispenserBehavior(ItemDispenserBehavior ...behaviors) {
        this.behaviors = behaviors;
    }

    @Override
    protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stackIn) {
        for (ItemDispenserBehavior behavior : behaviors) {
            ItemStack stackOut = ((ItemDispenserBehaviorAccessor) behavior).doDispenseSilently(pointer, stackIn);
            if (!(behavior instanceof FallibleItemDispenserBehavior) || ((FallibleItemDispenserBehaviorAccessor) behavior).isSuccessful()) {
                System.out.printf("success %s\n", behavior);
                this.success = true;
                return stackOut;
            } else {
                System.out.printf("fail %s\n", behavior);
            }
        }
        return stackIn;
    }
}
