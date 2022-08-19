package quickcarpet.mixin.stackableShulkerBoxesInHoppers.compat.lithium;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.utils.NBTHelper;

@Pseudo
@Mixin(targets = "me.jellysquid.mods.lithium.common.hopper.HopperHelper")
public class HopperHelperMixin {
    @Redirect(
        method = "tryMoveSingleItem(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/SidedInventory;Lnet/minecraft/item/ItemStack;ILnet/minecraft/util/math/Direction;)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxCount()I"))
    private static int quickcarpet$stackableShulkerBoxesInHoppers$getMaxCount$tryMoveSingleItem(ItemStack stack) {
        return NBTHelper.getMaxCountForRedstone(stack);
    }

    @Redirect(method = "determineComparatorUpdatePattern", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxCount()I"))
    private static int quickcarpet$stackableShulkerBoxesInHoppers$getMaxCount$determineComparatorUpdatePattern(ItemStack stack) {
        return NBTHelper.getMaxCountForRedstone(stack);
    }
}
