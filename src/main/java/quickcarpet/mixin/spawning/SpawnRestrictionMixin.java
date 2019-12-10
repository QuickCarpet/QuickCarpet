package quickcarpet.mixin.spawning;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnRestriction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.annotation.Feature;

@Feature("shulkerSpawningInEndCities")
@Mixin(SpawnRestriction.class)
public abstract class SpawnRestrictionMixin {
    @Inject(method = "getLocation", at = @At("HEAD"), cancellable = true)
    private static void shulkerOnGround(EntityType<?> type, CallbackInfoReturnable<SpawnRestriction.Location> cir) {
        if (type == EntityType.SHULKER) {
            cir.setReturnValue(SpawnRestriction.Location.ON_GROUND);
            cir.cancel();
        }
    }
}
