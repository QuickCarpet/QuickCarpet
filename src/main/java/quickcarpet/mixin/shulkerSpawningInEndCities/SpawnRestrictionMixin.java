package quickcarpet.mixin.shulkerSpawningInEndCities;

import net.minecraft.entity.SpawnRestriction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(SpawnRestriction.class)
public abstract class SpawnRestrictionMixin {
    @Redirect(method = "<clinit>", slice = @Slice(
        from = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityType;SHULKER:Lnet/minecraft/entity/EntityType;")
    ), at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/entity/SpawnRestriction$Location;NO_RESTRICTIONS:Lnet/minecraft/entity/SpawnRestriction$Location;",
        ordinal = 0
    ))
    private static SpawnRestriction.Location quickcarpet$shulkerSpawningInEndCities$shulkerOnGround() {
        return SpawnRestriction.Location.ON_GROUND;
    }
}
