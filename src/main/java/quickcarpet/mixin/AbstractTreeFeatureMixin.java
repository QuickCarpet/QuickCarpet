package quickcarpet.mixin;

import com.mojang.datafixers.Dynamic;
import net.minecraft.world.gen.feature.AbstractTreeFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Function;

@Mixin(AbstractTreeFeature.class)
public abstract class AbstractTreeFeatureMixin<T extends FeatureConfig> extends Feature<T> {
    public AbstractTreeFeatureMixin(Function<Dynamic<?>, ? extends T> function_1) {
        super(function_1);
    }

    //@Shadow protected abstract void generateBeeHive(IWorld iWorld_1, Random random_1, BlockPos blockPos_1, BlockBox blockBox_1, List<Set<BlockPos>> list_1, Biome biome_1);

    /*
    @Inject(method = "method_22362",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/structure/Structure;method_20532(Lnet/minecraft/world/IWorld;ILnet/minecraft/util/shape/VoxelSet;III)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void onManualTree(IWorld iWorld_1, ChunkGenerator<? extends ChunkGeneratorConfig> chunkGenerator_1, Random random_1, BlockPos blockPos_1, T class_4643_1, CallbackInfoReturnable<Boolean> cir, Set<BlockPos> positions, BlockBox box, boolean boolean_2, List<Set<BlockPos>> list) {
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
    */
}
