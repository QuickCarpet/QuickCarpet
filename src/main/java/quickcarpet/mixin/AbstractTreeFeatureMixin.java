package quickcarpet.mixin;

import com.mojang.datafixers.Dynamic;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorConfig;
import net.minecraft.world.gen.feature.AbstractTreeFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.settings.Settings;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

@Mixin(AbstractTreeFeature.class)
public abstract class AbstractTreeFeatureMixin<T extends FeatureConfig> extends Feature<T> {
    @Shadow protected abstract void generateBeeHive(IWorld iWorld_1, Random random_1, BlockPos blockPos_1, BlockBox blockBox_1, List<Set<BlockPos>> list_1, Biome biome_1);

    public AbstractTreeFeatureMixin(Function<Dynamic<?>, ? extends T> function_1, boolean boolean_1) {
        super(function_1, boolean_1);
    }

    @Inject(method = "method_22362",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/structure/Structure;method_20532(Lnet/minecraft/world/IWorld;ILnet/minecraft/util/shape/VoxelSet;III)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void onManualTree(IWorld world, ChunkGenerator<? extends ChunkGeneratorConfig> chunkGenerator, Random random, BlockPos pos, T featureConfig_1, boolean boolean_1, CallbackInfoReturnable<Boolean> cir, Set<BlockPos> positions, BlockBox box, boolean boolean_2, List<Set<BlockPos>> list) {
        if (boolean_1) return;
        Biome biome = world.getBiome(pos);
        if (!shouldGenerateHive(biome)) return;
        generateBeeHive(world, random, pos, box, list, biome);
    }

    private static boolean shouldGenerateHive(Biome biome) {
        switch(Settings.renewableBeeHives) {
            case NONE: return false;
            case ALL: return true;
            case FLOWER: return biome == Biomes.FLOWER_FOREST || biome == Biomes.SUNFLOWER_PLAINS;
            case FLOWER_AND_PLAINS: return biome == Biomes.FLOWER_FOREST || biome == Biomes.SUNFLOWER_PLAINS || biome == Biomes.PLAINS;
        }
        return false;
    }
}
