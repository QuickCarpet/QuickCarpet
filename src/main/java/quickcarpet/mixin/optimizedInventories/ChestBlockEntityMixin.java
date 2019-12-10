package quickcarpet.mixin.optimizedInventories;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Tickable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.annotation.Feature;
import quickcarpet.settings.Settings;
import quickcarpet.utils.InventoryOptimizer;
import quickcarpet.utils.extensions.OptimizedInventory;

import javax.annotation.Nullable;

@Feature("optimizedInventories")
@Mixin(ChestBlockEntity.class)
public abstract class ChestBlockEntityMixin extends LootableContainerBlockEntity implements OptimizedInventory, Tickable {
    @Shadow private DefaultedList<ItemStack> inventory;
    private InventoryOptimizer optimizer;

    protected ChestBlockEntityMixin(BlockEntityType<?> beType) {
        super(beType);
    }

    @Inject(method = "<init>(Lnet/minecraft/block/entity/BlockEntityType;)V", at = @At("RETURN"))
    private void onInit(BlockEntityType<?> beType, CallbackInfo ci) {
        reoptimize();
    }

    @Inject(method = "setInvStackList", at = @At("RETURN"))
    private void onSetStackList(DefaultedList<ItemStack> stackList, CallbackInfo ci) {
        reoptimize();
    }

    @Inject(method = "fromTag", at = @At("RETURN"))
    private void onDeserialize(CompoundTag tag, CallbackInfo ci) {
        reoptimize();
    }

    private void reoptimize() {
        optimizer = null;
        if (Settings.optimizedInventories && inventory.size() > 5) {
            optimizer = new InventoryOptimizer(inventory);
            optimizer.recalculate();
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (optimizer != null) optimizer.recalculate();
    }

    @Nullable
    public InventoryOptimizer getOptimizer() {
        return optimizer;
    }
}
