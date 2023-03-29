package quickcarpet.mixin.netherMaps;

import net.minecraft.item.map.MapState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.settings.Settings;

@Mixin(MapState.class)
public abstract class MapStateMixin extends PersistentState {
    @Redirect(method = "addIcon", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/item/map/MapState;dimension:Lnet/minecraft/registry/RegistryKey;"
    ))
    private RegistryKey<World> quickcarpet$netherMaps$getDimension(MapState state) {
        if (Settings.netherMaps && state.dimension == World.NETHER) {
            return World.OVERWORLD;
        }
        return state.dimension;
    }
}
