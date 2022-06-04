package quickcarpet.utils;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.StructureSpawns;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.ConfiguredStructureFeatures;
import quickcarpet.settings.Settings;

import javax.annotation.Nullable;
import java.util.Map;

public final class StructureSpawnOverrides {
    private StructureSpawnOverrides() {}

    public static boolean hasOverrides() {
        return Settings.huskSpawningInDesertPyramids || Settings.shulkerSpawningInEndCities;
    }

    @Nullable
    public static Map<SpawnGroup, StructureSpawns> getOverride(RegistryKey<ConfiguredStructureFeature<?, ?>> feature) {
        if (Settings.shulkerSpawningInEndCities && ConfiguredStructureFeatures.END_CITY.matchesKey(feature)) {
            return CarpetRegistry.END_CITY_SPAWN_MAP;
        }
        if (Settings.huskSpawningInDesertPyramids && ConfiguredStructureFeatures.DESERT_PYRAMID.matchesKey(feature)) {
            return CarpetRegistry.DESERT_PYRAMID_SPAWN_MAP;
        }
        return null;
    }
}
