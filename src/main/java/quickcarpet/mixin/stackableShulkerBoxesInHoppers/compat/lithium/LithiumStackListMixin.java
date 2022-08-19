package quickcarpet.mixin.stackableShulkerBoxesInHoppers.compat.lithium;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.utils.NBTHelper;

@Pseudo
@Mixin(targets = "me.jellysquid.mods.lithium.common.hopper.LithiumStackList")
public class LithiumStackListMixin {

    @Redirect(method = "<init>(Lnet/minecraft/util/collection/DefaultedList;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxCount()I"))
    private int quickcarpet$stackableShulkerBoxesInHoppers$getMaxCount$init(ItemStack stack) {
        return NBTHelper.getMaxCountForRedstone(stack);
    }

    @Redirect(method = "changedALot", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxCount()I"))
    private int quickcarpet$stackableShulkerBoxesInHoppers$getMaxCount$changedALot(ItemStack stack) {
        return NBTHelper.getMaxCountForRedstone(stack);
    }

    @Redirect(method = "beforeSlotCountChange", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxCount()I"))
    private int quickcarpet$stackableShulkerBoxesInHoppers$getMaxCount$beforeSlotCountChange(ItemStack stack) {
        return NBTHelper.getMaxCountForRedstone(stack);
    }

    @Redirect(method = "set", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxCount()I"))
    private int quickcarpet$stackableShulkerBoxesInHoppers$getMaxCount$set(ItemStack stack) {
        return NBTHelper.getMaxCountForRedstone(stack);
    }

    @Redirect(method = "calculateSignalStrength", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxCount()I"))
    private int quickcarpet$stackableShulkerBoxesInHoppers$getMaxCount$calculateSignalStrength(ItemStack stack) {
        return NBTHelper.getMaxCountForRedstone(stack);
    }
}
