package quickcarpet.utils;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.Weighting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import quickcarpet.mixin.accessor.SpawnHelperAccessor;
import quickcarpet.settings.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static quickcarpet.utils.Messenger.*;

public class SpawnUtils {
    public static Formatting creatureTypeColor(SpawnGroup type) {
        return switch (type) {
            case MONSTER -> Formatting.DARK_RED;
            case CREATURE -> Formatting.DARK_GREEN;
            case AMBIENT -> Formatting.DARK_GRAY;
            case WATER_CREATURE -> Formatting.BLUE;
            case WATER_AMBIENT -> Formatting.DARK_AQUA;
            default -> Formatting.WHITE;
        };
    }

    public static List<MutableText> list(ServerWorld world, BlockPos pos) {
        List<MutableText> result = new ArrayList<>();
        WorldChunk chunk = (WorldChunk) world.getChunk(pos);
        Settings.spawningAlgorithm.addSpawnListLocationInfo(result, chunk, pos.getX(), pos.getZ());
        for (SpawnGroup group : SpawnGroup.values()) {
            var entries = SpawnHelperAccessor.invokeGetSpawnEntries(world, world.getStructureAccessor(), world.getChunkManager().getChunkGenerator(), group, pos, null).getEntries();
            if (entries.isEmpty()) continue;
            result.add(t("command.spawn.list.group", group.getName()));
            int total = Weighting.getWeightSum(entries);
            for (var entry : entries) {
                EntityType<?> type = entry.type;
                int weight = entry.getWeight().getValue();
                double percentage = weight * 100.0 / total;
                var line = t("command.spawn.list.entry", format(type), dbl(percentage, getHeatmapColor(100 - percentage, 99)));
                var location = SpawnRestriction.getLocation(type);
                boolean canSpawn = location != null && SpawnHelper.canSpawn(location, world, pos, type);
                boolean fits = world.isSpaceEmpty(type.createSimpleBoundingBox(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D));
                line.append(", ");
                line.append(canSpawn
                    ? ts("command.spawn.list.entry.canSpawn", canSpawn && fits ? Formatting.GREEN : Formatting.GOLD)
                    : ts("command.spawn.list.entry.cantSpawn", Formatting.RED));
                line.append(", ");
                line.append(fits
                        ? ts("command.spawn.list.entry.fits", canSpawn && fits ? Formatting.GREEN : Formatting.GOLD)
                        : ts("command.spawn.list.entry.collides", Formatting.RED));
                result.add(line);
                result.add(t("command.spawn.list.entry.weight", weight));
                var pack = entry.minGroupSize == entry.maxGroupSize ? entry.minGroupSize : t("command.spawn.list.entry.packRange", entry.minGroupSize, entry.maxGroupSize);
                result.add(t("command.spawn.list.entry.pack", pack));
                if (!canSpawn || !fits) continue;
                double spawnChance = 0;
                Random random = new Random(0);
                for (int i = 0; i < 1000; i++) {
                    if (SpawnRestriction.canSpawn(type, world, SpawnReason.NATURAL, pos, random)) {
                        spawnChance++;
                    }
                }
                spawnChance /= 10;
                result.add(hoverText(
                    t("command.spawn.list.entry.chance", dbl(spawnChance, getHeatmapColor(100 - spawnChance, 99))),
                    t("command.spawn.list.entry.chance.hover"))
                );
            }
        }
        return result;
    }

    public static int getLowestBlock(Chunk chunk, int x, int z) {
        ChunkSection section = getLowestNonEmptySection(chunk);
        if (section == null) return chunk.getHeight();
        int localX = x & 0xf;
        int localZ = z & 0xf;
        for (int localY = 0; localY < 16; localY++) {
            if (!section.getBlockState(localX, localY, localZ).isAir()) {
                return section.getYOffset() + localY;
            }
        }
        int height = chunk.getHeight();
        BlockPos.Mutable pos = new BlockPos.Mutable(x, section.getYOffset() + 16, z);
        while (pos.getY() < height) {
            if (!chunk.getBlockState(pos).isAir()) return pos.getY();
            pos.move(0, 1, 0);
        }
        return height;
    }

    public static ChunkSection getLowestNonEmptySection(Chunk chunk) {
        ChunkSection[] sections = chunk.getSectionArray();
        for (ChunkSection section : sections) {
            if (!ChunkSection.isEmpty(section)) return section;
        }
        return null;
    }

    public static BlockPos getSpawnPosVanilla(World world, WorldChunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        int x = chunkPos.getStartX() + world.random.nextInt(16);
        int z = chunkPos.getStartZ() + world.random.nextInt(16);
        int maxY = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x, z) + 1;
        int y = MathHelper.nextBetween(world.random, world.getBottomY(), maxY);
        return new BlockPos(x, y, z);
    }

    public static BlockPos getSpawnPosLowestToHighest(World world, WorldChunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        int x = chunkPos.getStartX() + world.random.nextInt(16);
        int z = chunkPos.getStartZ() + world.random.nextInt(16);
        int maxY = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x, z) + 1;
        int minY = Math.min(getLowestBlock(chunk, x, z), maxY);
        int y = MathHelper.nextBetween(world.random, minY, maxY);
        return new BlockPos(x, y, z);
    }
}
