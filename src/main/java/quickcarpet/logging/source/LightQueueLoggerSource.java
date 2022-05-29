package quickcarpet.logging.source;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.light.LightingProvider;
import quickcarpet.QuickCarpetServer;
import quickcarpet.logging.Logger;
import quickcarpet.utils.extensions.ExtendedServerLightingProvider;

import java.util.HashMap;
import java.util.Map;

import static quickcarpet.utils.Messenger.*;

public class LightQueueLoggerSource implements LoggerSource {
    @Override
    public void tick() {
        for (var world : QuickCarpetServer.getMinecraftServer().getWorlds()) {
            LightingProvider provider = world.getLightingProvider();
            if (provider instanceof ExtendedServerLightingProvider ext) {
                ext.tickData();
            }
        }
    }

    @Override
    public void pull(Logger logger) {
        Map<ExtendedServerLightingProvider, ExtendedServerLightingProvider.Data> dataMap = new HashMap<>();
        logger.log((option, player) -> {
            ServerWorld world = (ServerWorld) player.world;
            LightingProvider provider = world.getLightingProvider();
            if (!(provider instanceof ExtendedServerLightingProvider ext)) return null;
            var data = dataMap.computeIfAbsent(ext, ExtendedServerLightingProvider::getData);
            var color = getHeatmapColor(data.enqueuedPerTick - data.executedPerTick, data.batchSize);
            return c(
                s("B:" + data.batchSize),
                s(" I:"), dbl(data.enqueuedPerTick, color),
                s("/t O:"), dbl(data.executedPerTick, color),
                s("/t Q:" + data.queueSize)
            );
        });
    }
}
