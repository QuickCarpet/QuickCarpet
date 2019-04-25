package quickcarpet.helper;

import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.BiomeSourceType;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSource;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSourceConfig;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.TheEndDimension;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.level.LevelGeneratorType;
import quickcarpet.mixin.skyblock.IProtoChunk;

import java.util.concurrent.ExecutionException;

public class SkyBlock {
    public static LevelGeneratorType LEVEL_GENERATOR_TYPE;

    public static ChunkGenerator<? extends ChunkGeneratorConfig> createOverworldChunkGenerator(World world) {
        ChunkGeneratorType<OverworldChunkGeneratorConfig, OverworldChunkGenerator> chunkGeneratorType = ChunkGeneratorType.SURFACE;
        BiomeSourceType<VanillaLayeredBiomeSourceConfig, VanillaLayeredBiomeSource> biomeSourceType = BiomeSourceType.VANILLA_LAYERED;
        OverworldChunkGeneratorConfig chunkGeneratorConfig = chunkGeneratorType.createSettings();
        VanillaLayeredBiomeSourceConfig biomeSourceConfig = (biomeSourceType.getConfig()).setLevelProperties(world.getLevelProperties()).setGeneratorSettings(chunkGeneratorConfig);
        return new SkyBlockOverworldGenerator(world, biomeSourceType.applyConfig(biomeSourceConfig), chunkGeneratorConfig);
    }

    public static ChunkGenerator<? extends ChunkGeneratorConfig> createNetherChunkGenerator(World world) {
        CavesChunkGeneratorConfig config = ChunkGeneratorType.CAVES.createSettings();
        config.setDefaultBlock(Blocks.NETHERRACK.getDefaultState());
        config.setDefaultFluid(Blocks.LAVA.getDefaultState());
        return new SkyBlockCavesGenerator(world, BiomeSourceType.FIXED.applyConfig((BiomeSourceType.FIXED.getConfig()).setBiome(Biomes.NETHER)), config);
    }

    public static ChunkGenerator<? extends ChunkGeneratorConfig> createEndChunkGenerator(World world) {
        FloatingIslandsChunkGeneratorConfig config = ChunkGeneratorType.FLOATING_ISLANDS.createSettings();
        config.setDefaultBlock(Blocks.END_STONE.getDefaultState());
        config.setDefaultFluid(Blocks.AIR.getDefaultState());
        config.withCenter(TheEndDimension.SPAWN_POINT);
        return new SkyBlockFloatingIslandsGenerator(world, BiomeSourceType.THE_END.applyConfig((BiomeSourceType.THE_END.getConfig()).method_9205(world.getSeed())), config);
    }

    public static void deleteBlocks(ProtoChunk chunk) {
        ChunkSection[] sections = chunk.getSectionArray();
        for (int i = 0; i < sections.length; i++) {
            sections[i] = WorldChunk.EMPTY_SECTION;
        }
        chunk.getBlockEntities().clear();
        ((IProtoChunk) chunk).getLightSources().clear();
    }

    public static void clearChunk(ProtoChunk chunk) {
        deleteBlocks(chunk);
        // erase entities
        chunk.getEntities().clear();
        try {
            ((ServerLightingProvider)chunk.getLightingProvider()).light(chunk, true).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static class SkyBlockOverworldGenerator extends OverworldChunkGenerator {

        public SkyBlockOverworldGenerator(IWorld world, BiomeSource biomeSource, OverworldChunkGeneratorConfig config) {
            super(world, biomeSource, config);
        }

        @Override
        public void populateEntities(ChunkRegion region) {
            ProtoChunk chunk = (ProtoChunk) region.getChunk(region.getCenterChunkX(), region.getCenterChunkZ());
            clearChunk(chunk);
        }
    }

    public static class SkyBlockCavesGenerator extends CavesChunkGenerator {
        public SkyBlockCavesGenerator(World world, BiomeSource biomeSource, CavesChunkGeneratorConfig config) {
            super(world, biomeSource, config);
        }

        @Override
        public void populateEntities(ChunkRegion region) {
            ProtoChunk chunk = (ProtoChunk) region.getChunk(region.getCenterChunkX(), region.getCenterChunkZ());
            clearChunk(chunk);
        }
    }

    public static class SkyBlockFloatingIslandsGenerator extends FloatingIslandsChunkGenerator {
        public SkyBlockFloatingIslandsGenerator(World world, BiomeSource biomeSource, FloatingIslandsChunkGeneratorConfig config) {
            super(world, biomeSource, config);
        }

        @Override
        public void populateEntities(ChunkRegion region) {
            ProtoChunk chunk = (ProtoChunk) region.getChunk(region.getCenterChunkX(), region.getCenterChunkZ());
            clearChunk(chunk);
        }
    }
}
