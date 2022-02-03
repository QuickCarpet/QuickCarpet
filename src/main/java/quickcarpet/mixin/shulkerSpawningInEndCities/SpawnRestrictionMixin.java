package quickcarpet.mixin.shulkerSpawningInEndCities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnRestriction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnRestriction.class)
public abstract class SpawnRestrictionMixin {
    // FIXME: There's probably a better way to do this
    @Inject(method = "getLocation", at = @At("HEAD"), cancellable = true)
    private static void quickcarpet$shulkerSpawningInEndCities$shulkerOnGround(EntityType<?> type, CallbackInfoReturnable<SpawnRestriction.Location> cir) {
        if (type == EntityType.SHULKER) {
            cir.setReturnValue(SpawnRestriction.Location.ON_GROUND);
            cir.cancel();
        }
    }
}
