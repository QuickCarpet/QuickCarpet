package quickcarpet.mixin.netherMaps;

import net.minecraft.item.map.MapState;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.PersistentState;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.settings.Settings;

@Mixin(MapState.class)
public abstract class MapStateMixin extends PersistentState {
    public MapStateMixin(String key) {
        super(key);
    }

    @Redirect(method = "addIcon", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/item/map/MapState;dimension:Lnet/minecraft/util/registry/RegistryKey;"
    ))
    private RegistryKey<DimensionType> redirectGetDimension(MapState state) {
        if (Settings.netherMaps && state.dimension == DimensionType.THE_NETHER_REGISTRY_KEY) {
            return DimensionType.OVERWORLD_REGISTRY_KEY;
        }
        return state.dimension;
    }
}
