package quickcarpet.mixin.spawning;

import net.minecraft.class_5311;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.SurfaceChunkGenerator;
import net.minecraft.world.gen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.settings.Settings;

import java.util.List;

@quickcarpet.annotation.Feature("shulkerSpawningInEndCities")
@Mixin(SurfaceChunkGenerator.class)
public abstract class SurfaceChunkGeneratorMixin extends ChunkGenerator {
    public SurfaceChunkGeneratorMixin(BiomeSource biomeSource, class_5311 arg) {
        super(biomeSource, arg);
    }

    @Inject(method = "getEntitySpawnList", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/chunk/ChunkGenerator;getEntitySpawnList(Lnet/minecraft/world/biome/Biome;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/util/math/BlockPos;)Ljava/util/List;"), cancellable = true)
    private void spawnShulkers(Biome biome, StructureAccessor accessor, SpawnGroup group, BlockPos pos, CallbackInfoReturnable<List<Biome.SpawnEntry>> cir) {
        if (Settings.shulkerSpawningInEndCities && group == SpawnGroup.MONSTER && accessor.method_28388(pos, true, StructureFeature.END_CITY).hasChildren()) {
            cir.setReturnValue(StructureFeature.END_CITY.getMonsterSpawns());
        }
    }
}
