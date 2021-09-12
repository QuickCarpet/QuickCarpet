package quickcarpet.helper;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.entity.EntityLike;
import quickcarpet.logging.LogParameter;
import quickcarpet.mixin.accessor.ServerChunkManagerAccessor;
import quickcarpet.mixin.accessor.SpawnDensityCapperAccessor;
import quickcarpet.mixin.accessor.SpawnDensityCapperDensityCapAccessor;
import quickcarpet.mixin.accessor.SpawnHelperInfoAccessor;

import java.util.*;

public class Mobcaps {
    public static Map<SpawnGroup, Pair<Integer, Integer>> getMobcaps(ServerWorld world) {
        int chunks = ((ServerChunkManagerAccessor) world.getChunkManager()).getTicketManager().getSpawningChunkCount();
        SpawnHelper.Info spawnInfo = world.getChunkManager().getSpawnInfo();
        if (spawnInfo == null) return Collections.emptyMap();
        Object2IntMap<SpawnGroup> mobs = spawnInfo.getGroupToCount();
        EnumMap<SpawnGroup, Pair<Integer, Integer>> mobcaps = new EnumMap<>(SpawnGroup.class);
        for (SpawnGroup category : SpawnGroup.values()) {
            if (category == SpawnGroup.MISC) continue;
            int cur = mobs.getOrDefault(category, 0);
            int max = chunks * category.getCapacity() / (17 * 17);
            mobcaps.put(category, new Pair<>(cur, max));
        }
        return mobcaps;
    }

    public static Object2FloatMap<SpawnGroup> getLocalMobcaps(ServerWorld world, ChunkPos pos) {
        SpawnHelper.Info info = world.getChunkManager().getSpawnInfo();
        SpawnDensityCapperAccessor densityCapper = (SpawnDensityCapperAccessor) ((SpawnHelperInfoAccessor) info).getDensityCapper();
        List<EntityLike> players = densityCapper.invokeGetMobSpawnablePlayers(pos.toLong());
        Object2FloatMap<SpawnGroup> mobcaps = new Object2FloatOpenHashMap<>();
        var caps = densityCapper.getPlayersToDensityCap();
        for (EntityLike player : players) {
            SpawnDensityCapperDensityCapAccessor cap = (SpawnDensityCapperDensityCapAccessor) caps.get(player);
            if (cap == null) continue;
            Object2FloatMap<SpawnGroup> spawnGroupsToDensity = cap.getSpawnGroupsToDensity();
            for (var e : spawnGroupsToDensity.object2FloatEntrySet()) {
                float previous = mobcaps.getOrDefault(e.getKey(), 1);
                mobcaps.put(e.getKey(), Math.min(previous, e.getFloatValue()));
            }
        }
        return mobcaps;
    }

    public static boolean isBelowCap(ServerChunkManager chunkManager, SpawnGroup group) {
        int count = chunkManager.getSpawnInfo().getGroupToCount().getInt(group);
        int chunks = ((ServerChunkManagerAccessor) chunkManager).getTicketManager().getSpawningChunkCount();
        int cap = chunks * group.getCapacity() / (17 * 17);
        return count < cap;
    }

    public static Collection<LogParameter> getCommandParameters(MinecraftServer server) {
        Collection<LogParameter> entries = new ArrayList<>();
        for (World world : server.getWorlds()) {
            RegistryKey<World> dimKey = world.getRegistryKey();
            for (SpawnGroup category : SpawnGroup.values()) {
                if (category == SpawnGroup.MISC) continue;
                entries.add(new LogParameter(
                        dimKey.getValue().toString() + "." + category.getName() + ".present",
                        () -> getMobcaps((ServerWorld) world).get(category).getLeft()));
                entries.add(new LogParameter(
                        dimKey.getValue().toString() + "." + category.getName() + ".limit",
                        () -> getMobcaps((ServerWorld) world).get(category).getRight()));
            }
        }
        return entries;
    }
}
