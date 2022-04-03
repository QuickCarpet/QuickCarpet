package quickcarpet.utils;

import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import quickcarpet.utils.Constants.SpawnCommand.Keys;

import java.util.List;

import static quickcarpet.utils.Messenger.t;
import static quickcarpet.utils.Messenger.tp;

public enum SpawningAlgorithm {
    VANILLA {
        @Override
        public BlockPos getSpawnPos(World world, WorldChunk chunk) {
            return SpawnUtils.getSpawnPosVanilla(world, chunk);
        }

        @Override
        public void addSpawnListLocationInfo(List<MutableText> result, WorldChunk chunk, int x, int z) {
            int heightmap = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x, z) + 1;
            BlockPos highestBlock = new BlockPos(x, heightmap, z);
            result.add(t(Keys.LIST_HIGHEST_BLOCK, tp(highestBlock, Formatting.AQUA)));
        }
    },
    SMART {
        @Override
        public BlockPos getSpawnPos(World world, WorldChunk chunk) {
            return SpawnUtils.getSpawnPosLowestToHighest(world, chunk);
        }

        @Override
        public void addSpawnListLocationInfo(List<MutableText> result, WorldChunk chunk, int x, int z) {
            int heightmap = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x, z) + 1;
            BlockPos highestBlock = new BlockPos(x, heightmap, z);
            result.add(t(Keys.LIST_HIGHEST_BLOCK, tp(highestBlock, Formatting.AQUA)));
            BlockPos lowestBlock = new BlockPos(x, SpawnUtils.getLowestBlock(chunk, x, z), z);
            result.add(t(Keys.LIST_LOWEST_BLOCK, tp(lowestBlock, Formatting.AQUA)));
        }
    };

    public abstract BlockPos getSpawnPos(World world, WorldChunk chunk);

    public abstract void addSpawnListLocationInfo(List<MutableText> result, WorldChunk chunk, int x, int z);
}
