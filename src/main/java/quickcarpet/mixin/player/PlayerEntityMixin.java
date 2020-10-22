package quickcarpet.mixin.player;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.api.annotation.Feature;
import quickcarpet.settings.Settings;

@Feature("portalCreativeDelay")
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Shadow public abstract Iterable<ItemStack> getItemsHand();

    @Inject(method = "getMaxNetherPortalTime", at = @At("HEAD"), cancellable = true)
    private void portalCreativeDelay(CallbackInfoReturnable<Integer> cir) {
        if (!Settings.portalCreativeDelay) return;
        int delay = 80;
        for (ItemStack heldItem : this.getItemsHand()) {
            if (heldItem.getItem() == Items.OBSIDIAN) delay = Integer.MAX_VALUE;
        }
        cir.setReturnValue(delay);
        cir.cancel();
    }
}
