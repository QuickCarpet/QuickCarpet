package quickcarpet.mixin.stackableShulkerBoxesInHoppers;

import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.settings.Settings;
import quickcarpet.utils.NBTHelper;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {
    @Inject(method = "canMergeItems", at = @At("HEAD"), cancellable = true)
    private static void quickcarpet$stackableShulkerBoxesInHoppers$canMergeShulkerBoxes(ItemStack first, ItemStack second, CallbackInfoReturnable<Boolean> cir) {
        if (!Settings.stackableShulkerBoxesInHoppers && (NBTHelper.isShulkerBox(first) || NBTHelper.isShulkerBox(second))) {
            cir.setReturnValue(false);
        }
    }
}
