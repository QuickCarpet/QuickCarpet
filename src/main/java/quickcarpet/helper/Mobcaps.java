package quickcarpet.helper;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import quickcarpet.QuickCarpet;
import quickcarpet.logging.Logger;
import quickcarpet.logging.loghelpers.LogParameter;
import quickcarpet.mixin.accessor.ServerChunkManagerAccessor;

import java.util.*;

public class Mobcaps {
    public static Map<SpawnGroup, Pair<Integer, Integer>> getMobcaps(RegistryKey<World> dimension) {
        return getMobcaps(QuickCarpet.minecraft_server.getWorld(dimension));
    }

    public static Map<SpawnGroup, Pair<Integer, Integer>> getMobcaps(ServerWorld world) {
        int chunks = ((ServerChunkManagerAccessor) world.getChunkManager()).getTicketManager().getSpawningChunkCount();
        SpawnHelper.Info spawnInfo = world.getChunkManager().getSpawnInfo();
        if (spawnInfo == null) return Collections.emptyMap();
        Object2IntMap<SpawnGroup> mobs = spawnInfo.getGroupToCount();
        EnumMap<SpawnGroup, Pair<Integer, Integer>> mobcaps = new EnumMap<>(SpawnGroup.class);
        for (SpawnGroup category : SpawnGroup.values()) {
            int cur = mobs.getOrDefault(category, 0);
            int max = chunks * category.getCapacity() / (17 * 17);
            mobcaps.put(category, new Pair<>(cur, max));
        }
        return mobcaps;
    }

    public static boolean isBelowCap(ServerChunkManager chunkManager, SpawnGroup group) {
        int count = chunkManager.getSpawnInfo().getGroupToCount().getInt(group);
        int chunks = ((ServerChunkManagerAccessor) chunkManager).getTicketManager().getSpawningChunkCount();
        int cap = chunks * group.getCapacity() / (17 * 17);
        return count < cap;
    }

    public static class LogCommandParameters extends AbstractMap<String, Integer> implements Logger.CommandParameters<Integer> {
        public static final LogCommandParameters INSTANCE = new LogCommandParameters();
        private LogCommandParameters() {}

        @Override
        public Set<Entry<String, Integer>> entrySet() {
            LinkedHashSet<Entry<String, Integer>> entries = new LinkedHashSet<>();
            for (World world : QuickCarpet.minecraft_server.getWorlds()) {
                RegistryKey<DimensionType> dimKey = world.getDimensionRegistryKey();
                for (SpawnGroup category : SpawnGroup.values()) {
                    entries.add(new LogParameter<>(
                            dimKey.getValue().toString() + "." + category.getName() + ".present",
                            () -> getMobcaps((ServerWorld) world).get(category).getLeft()));
                    entries.add(new LogParameter<>(
                            dimKey.getValue().toString() + "." + category.getName() + ".limit",
                            () -> getMobcaps((ServerWorld) world).get(category).getRight()));
                }
            }
            return entries;
        }
    }
}
