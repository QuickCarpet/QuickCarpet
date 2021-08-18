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
import net.minecraft.world.Heightmap;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.chunk.Chunk;
import quickcarpet.mixin.accessor.SpawnHelperAccessor;

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
        Chunk chunk = world.getChunk(pos);
        int heightmap = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, pos.getX(), pos.getZ()) + 1;
        BlockPos highestBlock = new BlockPos(pos.getX(), heightmap, pos.getZ());
        result.add(t("command.spawn.list.highestBlock", tp(highestBlock, Formatting.AQUA)));
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
}
