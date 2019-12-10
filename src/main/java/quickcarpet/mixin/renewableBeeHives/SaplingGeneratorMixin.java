package quickcarpet.mixin.renewableBeeHives;

import net.minecraft.block.BlockState;
import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.decorator.BeehiveTreeDecorator;
import net.minecraft.world.gen.decorator.TreeDecorator;
import net.minecraft.world.gen.feature.BranchedTreeFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.annotation.Feature;
import quickcarpet.settings.Settings;
import quickcarpet.utils.Reflection;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Feature("renewableBeeHives")
@Mixin(SaplingGenerator.class)
public abstract class SaplingGeneratorMixin {
    @Shadow @Nullable protected abstract ConfiguredFeature<BranchedTreeFeatureConfig, ?> createTreeFeature(Random var1);

    @Redirect(method = "generate", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/sapling/SaplingGenerator;createTreeFeature(Ljava/util/Random;)Lnet/minecraft/world/gen/feature/ConfiguredFeature;"))
    private ConfiguredFeature<BranchedTreeFeatureConfig, ?> generateHive(SaplingGenerator saplingGenerator, Random random, IWorld world, ChunkGenerator<?> chunkGenerator, BlockPos pos, BlockState blockState, Random random_1) {
        ConfiguredFeature<BranchedTreeFeatureConfig, ?> cf = this.createTreeFeature(random);
        if (cf == null) return null;
        Biome biome = world.getBiomeAccess().getBiome(pos);
        if (shouldGenerateHive(biome)) {
            List<TreeDecorator> decorators = new ArrayList<>(cf.config.decorators);
            decorators.add(new BeehiveTreeDecorator(getBeeHiveChance(biome)));
            Reflection.setFinalField(cf.config, "field_21290", List.class, decorators);
        }
        return cf;
    }

    private static float getBeeHiveChance(Biome biome) {
        return biome.getCategory() == Biome.Category.PLAINS ? 0.05f : 0.01f;
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
