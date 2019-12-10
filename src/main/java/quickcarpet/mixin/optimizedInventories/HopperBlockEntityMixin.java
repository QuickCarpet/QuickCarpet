package quickcarpet.mixin.optimizedInventories;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.annotation.Feature;
import quickcarpet.utils.InventoryOptimizer;
import quickcarpet.utils.extensions.OptimizedInventory;

import javax.annotation.Nullable;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin extends LootableContainerBlockEntity {
    protected HopperBlockEntityMixin(BlockEntityType<?> type) {
        super(type);
        throw new AbstractMethodError();
    }

    @Shadow
    private static ItemStack transfer(@Nullable Inventory from, Inventory to, ItemStack stack, int index, @Nullable Direction direction) {
        throw new AbstractMethodError();
    }

    @Feature("optimizedInventories")
    @Inject(method = "transfer(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/Direction;)Lnet/minecraft/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    private static void optimizedTransfer(Inventory from, Inventory to, ItemStack stack, Direction direction, CallbackInfoReturnable<ItemStack> cir) {
        if (to instanceof OptimizedInventory) {
            OptimizedInventory optoTo = (OptimizedInventory) to;
            InventoryOptimizer optimizer = optoTo.getOptimizer();
            if (optimizer == null) return;
            while (!stack.isEmpty()) {
                int index = optimizer.findInsertSlot(stack);
                if (index == -1) break;
                int count = stack.getCount();
                stack = transfer(from, to, stack, index, direction);
                if (stack.getCount() == count) break;
            }
            cir.setReturnValue(stack);
        }
    }
}
