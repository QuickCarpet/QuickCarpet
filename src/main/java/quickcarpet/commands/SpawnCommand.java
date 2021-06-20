package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import quickcarpet.helper.Mobcaps;
import quickcarpet.settings.Settings;
import quickcarpet.utils.SpawnTracker;

import java.util.Map;

import static net.minecraft.command.argument.BlockPosArgumentType.blockPos;
import static net.minecraft.command.argument.BlockPosArgumentType.getBlockPos;
import static net.minecraft.command.argument.DimensionArgumentType.dimension;
import static net.minecraft.command.argument.DimensionArgumentType.getDimensionArgument;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static quickcarpet.utils.Messenger.*;

public class SpawnCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = literal("spawn")
            .requires(s -> s.hasPermissionLevel(Settings.commandSpawn))
            .then(literal("mobcaps")
                .executes(c -> sendMobcaps(c.getSource(), null))
                .then(argument("dimension", dimension())
                    .executes(c -> sendMobcaps(c.getSource(), getDimensionArgument(c, "dimension")))))
            .then(literal("tracking")
                .executes(c -> sendTrackingReport(c.getSource()))
                .then(literal("start")
                    .executes(c -> startTracking(c.getSource(),null, null))
                    .then(argument("min", blockPos())
                    .then(argument("max", blockPos())
                        .executes(c -> startTracking(c.getSource(),
                            getBlockPos(c, "min"),
                            getBlockPos(c, "max")
                        )))))
                .then(literal("stop")
                    .executes(c -> stopTracking(c.getSource())))
            );
        dispatcher.register(builder);
    }

    private static int sendTrackingReport(ServerCommandSource source) {
        SpawnTracker tracker = SpawnTracker.getTracker(source);
        if (tracker == null) {
            m(source, ts("command.spawn.tracking.inactive", Formatting.GOLD));
            return 1;
        }
        tracker.sendReport();
        return 1;
    }

    private static int startTracking(ServerCommandSource source, BlockPos min, BlockPos max) {
        SpawnTracker tracker = SpawnTracker.getOrCreateTracker(source, min, max);
        if (tracker.isActive()) {
            m(source, ts("command.spawn.tracking.active", Formatting.GOLD));
            return 1;
        }
        tracker.start();
        m(source, ts("command.spawn.tracking.started", Formatting.DARK_GREEN));
        return 1;
    }

    private static int stopTracking(ServerCommandSource source) {
        SpawnTracker tracker = SpawnTracker.getTracker(source);
        if (tracker == null) {
            m(source, ts("command.spawn.tracking.active", Formatting.GOLD));
            return 1;
        }
        tracker.stop();
        m(source, ts("command.spawn.tracking.stopped", Formatting.DARK_GREEN));
        tracker.sendReport();
        return 1;
    }

    private static int sendMobcaps(ServerCommandSource source, ServerWorld dimension) {
        if (dimension == null) {
            dimension = source.getWorld();
        }
        Map<SpawnGroup, Pair<Integer, Integer>> mobcaps = Mobcaps.getMobcaps(dimension);
        m(source, t("command.spawn.mobcaps.title", dimension.getRegistryKey().getValue()));
        for (Map.Entry<SpawnGroup, Pair<Integer, Integer>> e : mobcaps.entrySet()) {
            SpawnGroup category = e.getKey();
            Pair<Integer, Integer> pair = e.getValue();
            int cur = pair.getLeft();
            int max = pair.getRight();
            Formatting color = cur >= max ? Formatting.RED : (cur * 10 >= max * 8 ? Formatting.YELLOW : Formatting.GREEN);
            Text capText = cur + max == 0 ? s("-/-", Formatting.DARK_GREEN) : formats("%d/%d", color, cur, max);
            m(source, t("command.spawn.mobcaps.line", category, capText));
        }
        return 1;
    }
}
