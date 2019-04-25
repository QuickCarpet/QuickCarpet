package quickcarpet.helper;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.BiomeSourceType;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSource;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSourceConfig;
import net.minecraft.world.chunk.*;
import net.minecraft.world.dimension.TheEndDimension;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.level.LevelGeneratorType;
import quickcarpet.mixin.skyblock.IProtoChunk;
import quickcarpet.mixin.skyblock.IStructurePiece;

import java.util.Random;
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

    private static void deleteBlocks(ProtoChunk chunk, IWorld world) {
        ChunkSection[] sections = chunk.getSectionArray();
        for (int i = 0; i < sections.length; i++) {
            sections[i] = WorldChunk.EMPTY_SECTION;
        }
        for (BlockPos bePos : chunk.getBlockEntityPositions()) {
            chunk.removeBlockEntity(bePos);
        }
        ((IProtoChunk) chunk).getLightSources().clear();
        processStronghold(chunk, world);
    }

    private static void processStronghold(ProtoChunk chunk, IWorld world) {
        for (long startPosLong : chunk.getStructureReferences("Stronghold")) {
            ChunkPos startPos = new ChunkPos(startPosLong);
            ProtoChunk startChunk = (ProtoChunk) world.getChunk(startPos.x, startPos.z, ChunkStatus.STRUCTURE_STARTS);
            StructureStart stronghold = startChunk.getStructureStart("Stronghold");
            ChunkPos pos = chunk.getPos();
            if (stronghold != null && stronghold.getBoundingBox().intersectsXZ(pos.getStartX(), pos.getStartZ(), pos.getEndX(), pos.getEndZ())) {
                for (StructurePiece piece : stronghold.getChildren()) {
                    if (piece instanceof StrongholdGenerator.PortalRoom) {
                        if (piece.getBoundingBox().intersectsXZ(pos.getStartX(), pos.getStartZ(), pos.getEndX(), pos.getEndZ())) {
                            generateStrongholdPortal(chunk, (StrongholdGenerator.PortalRoom) piece, new Random(startPosLong));
                        }
                    }
                }
            }
        }
    }

    private static BlockPos getBlockInStructurePiece(StructurePiece piece, int x, int y, int z) {
        IStructurePiece access = (IStructurePiece) piece;
        return new BlockPos(access.invokeApplyXTransform(x, z), access.invokeApplyYTransform(y), access.invokeApplyZTransform(x, z));
    }

    private static void setBlockInStructure(StructurePiece piece, ProtoChunk chunk, BlockState state, int x, int y, int z) {
        IStructurePiece access = (IStructurePiece) piece;
        BlockPos pos = getBlockInStructurePiece(piece, x, y, z);
        if (piece.getBoundingBox().contains(pos)) {
            BlockMirror mirror = access.getMirror();
            if (mirror != BlockMirror.NONE) state = state.mirror(mirror);
            BlockRotation rotation = piece.getRotation();
            if (rotation != BlockRotation.ROT_0) state = state.rotate(rotation);

            setBlockInChunk(chunk, pos, state);
        }
    }

    private static void setBlockInChunk(ProtoChunk chunk, BlockPos pos, BlockState state) {
        if (chunk.getPos().equals(new ChunkPos(pos))) {
            chunk.setBlockState(pos, state, false);
        }
    }

    private static void setBlockEntityInChunk(ProtoChunk chunk, BlockPos pos, CompoundTag tag) {
        if (chunk.getPos().equals(new ChunkPos(pos))) {
            tag.putInt("x", pos.getX());
            tag.putInt("y", pos.getY());
            tag.putInt("z", pos.getZ());
            System.out.println(tag);
            chunk.addPendingBlockEntityTag(tag);
        }
    }

    private static void generateStrongholdPortal(ProtoChunk chunk, StrongholdGenerator.PortalRoom room, Random random) {
        BlockState northFrame = Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.NORTH);
        BlockState southFrame = Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.SOUTH);
        BlockState eastFrame = Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.EAST);
        BlockState westFrame = Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.WEST);
        boolean completelyFilled = true;
        boolean[] framesFilled = new boolean[12];

        for(int i = 0; i < framesFilled.length; ++i) {
            framesFilled[i] = random.nextFloat() > 0.9F;
            completelyFilled &= framesFilled[i];
        }
        setBlockInStructure(room, chunk, northFrame.with(EndPortalFrameBlock.EYE, framesFilled[0]), 4, 3, 8);
        setBlockInStructure(room, chunk, northFrame.with(EndPortalFrameBlock.EYE, framesFilled[1]), 5, 3, 8);
        setBlockInStructure(room, chunk, northFrame.with(EndPortalFrameBlock.EYE, framesFilled[2]), 6, 3, 8);
        setBlockInStructure(room, chunk, southFrame.with(EndPortalFrameBlock.EYE, framesFilled[3]), 4, 3, 12);
        setBlockInStructure(room, chunk, southFrame.with(EndPortalFrameBlock.EYE, framesFilled[4]), 5, 3, 12);
        setBlockInStructure(room, chunk, southFrame.with(EndPortalFrameBlock.EYE, framesFilled[5]), 6, 3, 12);
        setBlockInStructure(room, chunk, eastFrame.with(EndPortalFrameBlock.EYE, framesFilled[6]), 3, 3, 9);
        setBlockInStructure(room, chunk, eastFrame.with(EndPortalFrameBlock.EYE, framesFilled[7]), 3, 3, 10);
        setBlockInStructure(room, chunk, eastFrame.with(EndPortalFrameBlock.EYE, framesFilled[8]), 3, 3, 11);
        setBlockInStructure(room, chunk, westFrame.with(EndPortalFrameBlock.EYE, framesFilled[9]), 7, 3, 9);
        setBlockInStructure(room, chunk, westFrame.with(EndPortalFrameBlock.EYE, framesFilled[10]), 7, 3, 10);
        setBlockInStructure(room, chunk, westFrame.with(EndPortalFrameBlock.EYE, framesFilled[11]), 7, 3, 11);
        if (completelyFilled) {
            BlockState portal = Blocks.END_PORTAL.getDefaultState();
            setBlockInStructure(room, chunk, portal, 4, 3, 9);
            setBlockInStructure(room, chunk, portal, 5, 3, 9);
            setBlockInStructure(room, chunk, portal, 6, 3, 9);
            setBlockInStructure(room, chunk, portal, 4, 3, 10);
            setBlockInStructure(room, chunk, portal, 5, 3, 10);
            setBlockInStructure(room, chunk, portal, 6, 3, 10);
            setBlockInStructure(room, chunk, portal, 4, 3, 11);
            setBlockInStructure(room, chunk, portal, 5, 3, 11);
            setBlockInStructure(room, chunk, portal, 6, 3, 11);
        }
        BlockPos spawnerPos = getBlockInStructurePiece(room, 5, 3, 6);
        setBlockInChunk(chunk, spawnerPos, Blocks.SPAWNER.getDefaultState());
        CompoundTag spawnerTag = new CompoundTag();
        spawnerTag.putString("id", "minecraft:mob_spawner");
        ListTag spawnPotentials = new ListTag();
        spawnerTag.put("SpawnPotentials", spawnPotentials);
        CompoundTag spawnEntry = new CompoundTag();
        spawnPotentials.addTag(0, spawnEntry);
        CompoundTag entity = new CompoundTag();
        spawnEntry.put("Entity", entity);
        entity.putString("id", "minecraft:silverfish");
        spawnEntry.putInt("Weight", 1);
        spawnerTag.put("SpawnData", entity.copy());
        setBlockEntityInChunk(chunk, spawnerPos, spawnerTag);
    }

    private static void clearChunk(ProtoChunk chunk, IWorld world) {
        deleteBlocks(chunk, world);
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
            clearChunk(chunk, world);
        }
    }

    public static class SkyBlockCavesGenerator extends CavesChunkGenerator {
        public SkyBlockCavesGenerator(World world, BiomeSource biomeSource, CavesChunkGeneratorConfig config) {
            super(world, biomeSource, config);
        }

        @Override
        public void populateEntities(ChunkRegion region) {
            ProtoChunk chunk = (ProtoChunk) region.getChunk(region.getCenterChunkX(), region.getCenterChunkZ());
            clearChunk(chunk, world);
        }
    }

    public static class SkyBlockFloatingIslandsGenerator extends FloatingIslandsChunkGenerator {
        public SkyBlockFloatingIslandsGenerator(World world, BiomeSource biomeSource, FloatingIslandsChunkGeneratorConfig config) {
            super(world, biomeSource, config);
        }

        @Override
        public void populateEntities(ChunkRegion region) {
            ProtoChunk chunk = (ProtoChunk) region.getChunk(region.getCenterChunkX(), region.getCenterChunkZ());
            clearChunk(chunk, world);
        }
    }
}
