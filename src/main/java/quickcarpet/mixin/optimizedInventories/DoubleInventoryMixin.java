package quickcarpet.mixin.optimizedInventories;

import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.annotation.Feature;
import quickcarpet.utils.DoubleInventoryOptimizer;
import quickcarpet.utils.InventoryOptimizer;
import quickcarpet.utils.extensions.OptimizedInventory;

import javax.annotation.Nullable;

@Feature("optimizedInventories")
@Mixin(DoubleInventory.class)
public abstract class DoubleInventoryMixin implements OptimizedInventory {
    @Shadow @Final private Inventory first;
    @Shadow @Final private Inventory second;

    private InventoryOptimizer optimizer;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(Inventory first, Inventory second, CallbackInfo ci) {
        if(first instanceof OptimizedInventory && ((OptimizedInventory) first).getOptimizer() != null
                && second instanceof OptimizedInventory && ((OptimizedInventory) second).getOptimizer() != null) {
            optimizer = new DoubleInventoryOptimizer(((OptimizedInventory) first), ((OptimizedInventory) second));
            optimizer.recalculate();
        }
    }

    @Override
    @Nullable
    public InventoryOptimizer getOptimizer() {
        return optimizer;
    }
}
