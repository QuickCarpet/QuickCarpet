package quickcarpet.mixin.hopperCounters;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.QuickCarpetServer;
import quickcarpet.feature.HopperCounter;
import quickcarpet.settings.Settings;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    @Shadow public abstract ItemStack getStack();

    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;discard()V"))
    private void quickcarpet$hopperCounters$countDestroy(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!world.isClient() && Settings.hopperCounters) {
            MinecraftServer server = QuickCarpetServer.getMinecraftServer();
            if (source == DamageSource.CACTUS) {
                HopperCounter.getCounter(HopperCounter.Key.CACTUS).add(server, getStack());
            } else {
                HopperCounter.getCounter(HopperCounter.Key.DESTROY).add(server, getStack());
            }
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;discard()V", ordinal = 1))
    private void quickcarpet$hopperCounters$despawn(CallbackInfo ci) {
        if (Settings.hopperCounters) {
            HopperCounter.getCounter(HopperCounter.Key.DESPAWN).add(QuickCarpetServer.getMinecraftServer(), getStack());
        }
    }

    @Inject(method = "onPlayerCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setCount(I)V", shift = At.Shift.AFTER))
    private void quickcarpet$hopperCounters$pickup(PlayerEntity player, CallbackInfo ci) {
        if (Settings.hopperCounters) {
            HopperCounter.getCounter(HopperCounter.Key.PICKUP).add(QuickCarpetServer.getMinecraftServer(), getStack());
        }
    }

    @Inject(method = "onPlayerCollision", at = @At(value = "TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void quickcarpet$hopperCounters$pickup(PlayerEntity player, CallbackInfo ci, ItemStack stack, Item item, int countBefore) {
        if (Settings.hopperCounters) {
            int difference = countBefore - stack.getCount();
            if (difference > 0) {
                HopperCounter.getCounter(HopperCounter.Key.PICKUP).add(QuickCarpetServer.getMinecraftServer(), new ItemStack(item, difference));
            }
        }
    }
}
