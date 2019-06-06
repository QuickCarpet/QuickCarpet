package quickcarpet.mixin;

import net.minecraft.block.dispenser.BlockPlacementDispenserBehavior;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.settings.Settings;

@Mixin(BlockPlacementDispenserBehavior.class)
public class BlockPlacementDispenserBehaviorMixin {
    @Redirect(method = "dispenseSilently", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V"))
    private void fixDoubleDecrement(ItemStack stack, int amount) {
        if (!Settings.stackableShulkerBoxes) stack.decrement(amount);
    }
}
