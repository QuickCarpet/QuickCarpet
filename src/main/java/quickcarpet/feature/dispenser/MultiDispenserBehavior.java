package quickcarpet.feature.dispenser;

import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointer;
import quickcarpet.mixin.accessor.ItemDispenserBehaviorAccessor;

import java.util.List;

public class MultiDispenserBehavior extends FallibleItemDispenserBehavior {
    private final DispenserBehavior[] behaviors;

    public MultiDispenserBehavior(DispenserBehavior...behaviors) {
        this.behaviors = behaviors;
    }

    public static DispenserBehavior of(List<DispenserBehavior> behaviors) {
        if (behaviors.size() == 1) return behaviors.get(0);
        return new MultiDispenserBehavior(behaviors.toArray(new DispenserBehavior[0]));
    }

    @Override
    protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stackIn) {
        for (DispenserBehavior behavior : behaviors) {
            if (behavior == null) continue;
            ItemStack stackOut = ((ItemDispenserBehaviorAccessor) behavior).doDispenseSilently(pointer, stackIn);
            if (!(behavior instanceof FallibleItemDispenserBehavior fallible) || fallible.isSuccess()) {
                this.setSuccess(true);
                return stackOut;
            }
        }
        return stackIn;
    }
}
