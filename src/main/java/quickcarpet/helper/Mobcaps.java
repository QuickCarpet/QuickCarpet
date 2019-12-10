package quickcarpet.helper;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.entity.EntityCategory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.world.dimension.DimensionType;
import quickcarpet.QuickCarpet;
import quickcarpet.logging.Logger;
import quickcarpet.logging.loghelpers.LogParameter;
import quickcarpet.mixin.accessor.ServerChunkManagerAccessor;

import java.util.*;

public class Mobcaps {
    public static Map<EntityCategory, Pair<Integer, Integer>> getMobcaps(DimensionType dimensionType) {
        return getMobcaps(QuickCarpet.minecraft_server.getWorld(dimensionType));
    }

    public static Map<EntityCategory, Pair<Integer, Integer>> getMobcaps(ServerWorld world) {
        int chunks = ((ServerChunkManagerAccessor) world.getChunkManager()).getTicketManager().getLevelCount();
        Object2IntMap<EntityCategory> mobs = world.getMobCountsByCategory();
        EnumMap<EntityCategory, Pair<Integer, Integer>> mobcaps = new EnumMap<>(EntityCategory.class);
        for (EntityCategory category : EntityCategory.values()) {
            int cur = mobs.getOrDefault(category, 0);
            int max = chunks * category.getSpawnCap() / (17 * 17);
            mobcaps.put(category, new Pair<>(cur, max));
        }
        return mobcaps;
    }

    public static class LogCommandParameters extends AbstractMap<String, Integer> implements Logger.CommandParameters<Integer> {
        public static final LogCommandParameters INSTANCE = new LogCommandParameters();
        private LogCommandParameters() {}

        @Override
        public Set<Entry<String, Integer>> entrySet() {
            LinkedHashSet<Entry<String, Integer>> entries = new LinkedHashSet<>();
            for (DimensionType dim : DimensionType.getAll()) {
                for (EntityCategory category : EntityCategory.values()) {
                    entries.add(new LogParameter<>(
                            dim.toString() + "." + category.getName() + ".present",
                            () -> getMobcaps(dim).get(category).getLeft()));
                    entries.add(new LogParameter<>(
                            dim.toString() + "." + category.getName() + ".limit",
                            () -> getMobcaps(dim).get(category).getRight()));
                }
            }
            return entries;
        }
    }
}
