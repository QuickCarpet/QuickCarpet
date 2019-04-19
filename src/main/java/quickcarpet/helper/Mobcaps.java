package quickcarpet.helper;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.entity.EntityCategory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.world.dimension.DimensionType;
import quickcarpet.QuickCarpet;
import quickcarpet.mixin.IServerChunkManager;

import java.util.EnumMap;
import java.util.Map;

public class Mobcaps {
    public static Map<EntityCategory, Pair<Integer, Integer>> getMobcaps(DimensionType dimensionType) {
        return getMobcaps(QuickCarpet.minecraft_server.getWorld(dimensionType));
    }

    public static Map<EntityCategory, Pair<Integer, Integer>> getMobcaps(ServerWorld world) {
        int chunks = ((IServerChunkManager) world.method_14178()).getTicketManager().getLevelCount();
        Object2IntMap<EntityCategory> mobs = world.getMobCountsByCategory();
        EnumMap<EntityCategory, Pair<Integer, Integer>> mobcaps = new EnumMap<>(EntityCategory.class);
        for (EntityCategory category : EntityCategory.values()) {
            int cur = mobs.getOrDefault(category, 0);
            int max = chunks * category.getSpawnCap() / (17 * 17);
            mobcaps.put(category, new Pair<>(cur, max));
        }
        return mobcaps;
    }
}
