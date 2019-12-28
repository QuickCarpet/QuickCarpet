package quickcarpet.mixin.netherMaps;

import net.minecraft.item.map.MapState;
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
        target = "Lnet/minecraft/item/map/MapState;dimension:Lnet/minecraft/world/dimension/DimensionType;"
    ))
    private DimensionType redirectGetDimension(MapState state) {
        if (Settings.netherMaps && state.dimension == DimensionType.THE_NETHER) {
            return DimensionType.OVERWORLD;
        }
        return state.dimension;
    }
}
