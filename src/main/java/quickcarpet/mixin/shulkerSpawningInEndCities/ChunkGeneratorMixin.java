package quickcarpet.mixin.shulkerSpawningInEndCities;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.StructureSpawns;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.ConfiguredStructureFeatures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.settings.Settings;
import quickcarpet.utils.CarpetRegistry;

import java.util.Map;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {
    @Redirect(method = "getEntitySpawnList", at = @At(value = "FIELD", target = "Lnet/minecraft/world/gen/feature/ConfiguredStructureFeature;field_37143:Ljava/util/Map;", ordinal = 0))
    private Map<SpawnGroup, StructureSpawns> quickcarpet$overrideSpawns(ConfiguredStructureFeature<?, ?> feature, RegistryEntry<Biome> biome, StructureAccessor accessor) {
        var spawns = feature.field_37143;
        if (!Settings.shulkerSpawningInEndCities || spawns.containsKey(SpawnGroup.MONSTER)) return spawns;
        var registry = accessor.method_41036().get(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY);
        var key = registry.getKey(feature);
        if (key.isEmpty()) return spawns;
        if (ConfiguredStructureFeatures.END_CITY.matchesKey(key.get())) {
            return CarpetRegistry.END_CITY_SPAWN_MAP;
        }
        return spawns;
    }
}
