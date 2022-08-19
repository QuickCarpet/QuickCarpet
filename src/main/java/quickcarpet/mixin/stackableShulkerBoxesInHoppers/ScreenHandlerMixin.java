package quickcarpet.mixin.stackableShulkerBoxesInHoppers;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.settings.Settings;
import quickcarpet.utils.NBTHelper;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {
    @Redirect(method = "calculateComparatorOutput(Lnet/minecraft/inventory/Inventory;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxCount()I"))
    private static int quickcarpet$stackableShulkerBoxesInHoppers$getMaxCount(ItemStack stack) {
        return !Settings.stackableShulkerBoxesInHoppers && NBTHelper.isShulkerBox(stack) ? 1 : stack.getMaxCount();
    }
}
