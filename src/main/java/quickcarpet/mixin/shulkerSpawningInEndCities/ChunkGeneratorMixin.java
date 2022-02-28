package quickcarpet.mixin.shulkerSpawningInEndCities;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.StructureSpawns;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import quickcarpet.settings.Settings;
import quickcarpet.utils.CarpetRegistry;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {
    @ModifyVariable(method = "getEntitySpawnList", at = @At(value = "STORE", remap = false))
    private StructureSpawns quickcarpet$shulkerSpawningInEndCities(StructureSpawns spawns, RegistryEntry<Biome> biome, StructureAccessor accessor, SpawnGroup group) {
        if (spawns == null && Settings.shulkerSpawningInEndCities && group == SpawnGroup.MONSTER) {
            return CarpetRegistry.END_CITY_SPAWNS;
        }
        return spawns;
    }
}
