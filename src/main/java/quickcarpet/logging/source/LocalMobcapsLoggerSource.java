package quickcarpet.logging.source;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.util.math.ChunkPos;
import quickcarpet.QuickCarpetServer;
import quickcarpet.helper.Mobcaps;
import quickcarpet.logging.Logger;
import quickcarpet.utils.SpawnUtils;

import java.util.ArrayList;
import java.util.List;

import static quickcarpet.utils.Messenger.c;
import static quickcarpet.utils.Messenger.s;

public class LocalMobcapsLoggerSource implements LoggerSource {
    @Override
    public void pull(Logger logger) {
        logger.log((option, player) -> {
            ServerWorld world = (ServerWorld) player.world;
            return formatMobcaps(Mobcaps.getLocalMobcaps(world, new ChunkPos(player.getBlockPos())));
        }, () -> Mobcaps.getCommandParameters(QuickCarpetServer.getMinecraftServer()));
    }

    private static MutableText formatMobcaps(Object2FloatMap<SpawnGroup> mobcaps) {
        if (mobcaps.isEmpty()) return null;
        List<MutableText> components = new ArrayList<>();
        for (var e : mobcaps.object2FloatEntrySet()) {
            components.add(s(String.format("%.3f", e.getFloatValue()), SpawnUtils.creatureTypeColor(e.getKey())));
            components.add(s(" "));
        }
        components.remove(components.size() - 1);
        return c(components.toArray(new MutableText[0]));
    }
}
