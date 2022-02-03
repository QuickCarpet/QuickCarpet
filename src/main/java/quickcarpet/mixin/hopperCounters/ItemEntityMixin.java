package quickcarpet.mixin.hopperCounters;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.QuickCarpetServer;
import quickcarpet.helper.HopperCounter;
import quickcarpet.settings.Settings;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    @Shadow public abstract ItemStack getStack();

    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;discard()V"))
    private void quickcarpet$hopperCounters$countCactus(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!world.isClient() && Settings.hopperCounters && source == DamageSource.CACTUS) {
            HopperCounter.getCounter(HopperCounter.Key.CACTUS).add(QuickCarpetServer.getMinecraftServer(), getStack());
        }
    }
}
