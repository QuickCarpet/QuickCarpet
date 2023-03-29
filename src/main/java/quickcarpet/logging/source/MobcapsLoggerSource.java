package quickcarpet.logging.source;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.world.World;
import quickcarpet.QuickCarpetServer;
import quickcarpet.logging.Logger;
import quickcarpet.utils.Mobcaps;
import quickcarpet.utils.SpawnUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static quickcarpet.utils.Messenger.*;

public class MobcapsLoggerSource implements LoggerSource {
    @Override
    public void pull(Logger logger) {
        logger.log((option, player) -> {
            ServerWorld world = (ServerWorld) player.world;
            MinecraftServer server = world.getServer();
            RegistryKey<World> dim = switch (option) {
                case "overworld" -> World.OVERWORLD;
                case "nether" -> World.NETHER;
                case "end" -> World.END;
                default -> world.getRegistryKey();
            };
            return formatMobcaps(Mobcaps.getMobcaps(server.getWorld(dim)));
        }, () -> Mobcaps.getCommandParameters(QuickCarpetServer.getMinecraftServer()));
    }

    private static MutableText formatMobcaps(Map<SpawnGroup, Pair<Integer, Integer>> mobcaps) {
        List<MutableText> components = new ArrayList<>();
        for (Map.Entry<SpawnGroup, Pair<Integer, Integer>> e : mobcaps.entrySet()) {
            Pair<Integer, Integer> pair = e.getValue();
            int actual = pair.getLeft();
            int limit = pair.getRight();
            if (actual + limit == 0) {
                components.add(s("-/-", Formatting.GRAY));
            } else {
                components.add(s(Integer.toString(actual), getHeatmapColor(actual, limit)));
                components.add(s("/", Formatting.GRAY));
                components.add(s(Integer.toString(limit), SpawnUtils.creatureTypeColor(e.getKey())));
            }
            components.add(s(" "));
        }
        components.remove(components.size() - 1);
        return c(components.toArray(new MutableText[0]));
    }
}
