package quickcarpet.mixin.stackableShulkerBoxes;

import fi.dy.masa.malilib.util.InventoryUtils;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.settings.Settings;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow
    public abstract Item getItem();

    @Inject(method = "getMaxCount", at = @At("HEAD"), cancellable = true)
    private void allowShulkerBoxStackingInInventory(CallbackInfoReturnable<Integer> cir) {
        if (Settings.stackableShulkerBoxesInInventories && this.getItem() instanceof BlockItem Item) {
            if (Item.getBlock() instanceof ShulkerBoxBlock && !InventoryUtils.shulkerBoxHasItems((ItemStack) (Object) this)) {
                cir.setReturnValue(64);
            }
        }
    }
}
