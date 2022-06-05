package quickcarpet.mixin.structureSpawnOverrides;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.StructureSpawns;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.utils.StructureSpawnOverrides;

import java.util.Map;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {
    @Redirect(method = "getEntitySpawnList", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/structure/Structure;getStructureSpawns()Ljava/util/Map;", ordinal = 0))
    private Map<SpawnGroup, StructureSpawns> quickcarpet$overrideSpawns(Structure feature, RegistryEntry<Biome> biome, StructureAccessor accessor) {
        var spawns = feature.getStructureSpawns();
        if (!StructureSpawnOverrides.hasOverrides() || spawns.containsKey(SpawnGroup.MONSTER)) return spawns;
        var registry = accessor.getRegistryManager().get(Registry.STRUCTURE_KEY);
        var key = registry.getKey(feature);
        if (key.isEmpty()) return spawns;
        var override = StructureSpawnOverrides.getOverride(key.get());
        return override != null ? override : spawns;
    }
}
