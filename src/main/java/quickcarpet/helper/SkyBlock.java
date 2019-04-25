package quickcarpet.helper;

import net.minecraft.world.ChunkRegion;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.BiomeSourceType;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSource;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSourceConfig;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.level.LevelGeneratorType;
import quickcarpet.mixin.IProtoChunk;

public class SkyBlock {
    public static LevelGeneratorType LEVEL_GENERATOR_TYPE;

    public static ChunkGenerator<? extends ChunkGeneratorConfig> createOverworldChunkGenerator(World world) {
        ChunkGeneratorType<OverworldChunkGeneratorConfig, OverworldChunkGenerator> chunkGeneratorType = ChunkGeneratorType.SURFACE;
        BiomeSourceType<VanillaLayeredBiomeSourceConfig, VanillaLayeredBiomeSource> biomeSourceType = BiomeSourceType.VANILLA_LAYERED;
        OverworldChunkGeneratorConfig chunkGeneratorConfig = chunkGeneratorType.createSettings();
        VanillaLayeredBiomeSourceConfig biomeSourceConfig = (biomeSourceType.getConfig()).setLevelProperties(world.getLevelProperties()).setGeneratorSettings(chunkGeneratorConfig);
        return new SkyBlockChunkGenerator(world, biomeSourceType.applyConfig(biomeSourceConfig), chunkGeneratorConfig);
    }

    public static void deleteBlocks(ProtoChunk chunk) {
        ChunkSection[] sections = chunk.getSectionArray();
        for (int i = 0; i < sections.length; i++) {
            sections[i] = WorldChunk.EMPTY_SECTION;
        }
        chunk.getBlockEntities().clear();
        ((IProtoChunk) chunk).getLightSources().clear();
    }

    public static class SkyBlockChunkGenerator extends OverworldChunkGenerator {

        public SkyBlockChunkGenerator(IWorld world, BiomeSource biomeSource, OverworldChunkGeneratorConfig chunkGeneratorConfig) {
            super(world, biomeSource, chunkGeneratorConfig);
        }

        @Override
        public void generateFeatures(ChunkRegion region) {
            ProtoChunk chunk = (ProtoChunk) region.getChunk(region.getCenterChunkX(), region.getCenterChunkZ());
            deleteBlocks(chunk);
        }

        @Override
        public void populateEntities(ChunkRegion region) {
            ProtoChunk chunk = (ProtoChunk) region.getChunk(region.getCenterChunkX(), region.getCenterChunkZ());
            // erase entities
            chunk.getEntities().clear();
        }
    }
}
