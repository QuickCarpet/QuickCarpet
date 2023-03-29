package quickcarpet.utils;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.StructureSpawns;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureKeys;
import quickcarpet.settings.Settings;

import javax.annotation.Nullable;
import java.util.Map;

public final class StructureSpawnOverrides {
    private StructureSpawnOverrides() {}

    public static boolean hasOverrides() {
        return Settings.huskSpawningInDesertPyramids || Settings.shulkerSpawningInEndCities;
    }

    @Nullable
    public static Map<SpawnGroup, StructureSpawns> getOverride(RegistryKey<Structure> feature) {
        if (Settings.shulkerSpawningInEndCities && StructureKeys.END_CITY.equals(feature)) {
            return CarpetRegistry.END_CITY_SPAWN_MAP;
        }
        if (Settings.huskSpawningInDesertPyramids && StructureKeys.DESERT_PYRAMID.equals(feature)) {
            return CarpetRegistry.DESERT_PYRAMID_SPAWN_MAP;
        }
        return null;
    }
}
