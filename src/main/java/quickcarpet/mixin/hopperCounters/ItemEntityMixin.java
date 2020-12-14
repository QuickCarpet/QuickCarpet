package quickcarpet.mixin.hopperCounters;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.QuickCarpetServer;
import quickcarpet.helper.HopperCounter;
import quickcarpet.settings.Settings;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Shadow public abstract ItemStack getStack();

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;remove()V"))
    private void countCactus(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (Settings.hopperCounters && source == DamageSource.CACTUS) {
            HopperCounter.getCounter(HopperCounter.Key.CACTUS).add(QuickCarpetServer.getMinecraftServer(), getStack());
        }
    }
}
