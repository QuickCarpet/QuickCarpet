package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import quickcarpet.helper.Mobcaps;
import quickcarpet.settings.Settings;
import quickcarpet.utils.Constants.SpawnCommand.Keys;
import quickcarpet.utils.SpawnTracker;
import quickcarpet.utils.SpawnUtils;

import static net.minecraft.command.argument.BlockPosArgumentType.blockPos;
import static net.minecraft.command.argument.BlockPosArgumentType.getBlockPos;
import static net.minecraft.command.argument.DimensionArgumentType.dimension;
import static net.minecraft.command.argument.DimensionArgumentType.getDimensionArgument;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static quickcarpet.utils.Constants.SpawnCommand.Texts.*;
import static quickcarpet.utils.Messenger.*;

public class SpawnCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var spawn = literal("spawn")
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
            )
            .then(literal("list")
                .executes(c -> list(c.getSource()))
                .then(argument("pos", blockPos())
                    .executes(c -> list(c.getSource(), getBlockPos(c, "pos")))));
        dispatcher.register(spawn);
    }

    private static int sendTrackingReport(ServerCommandSource source) {
        SpawnTracker tracker = SpawnTracker.getTracker(source);
        if (tracker == null) {
            m(source, TRACKING_INACTIVE);
            return 1;
        }
        tracker.sendReport();
        return 1;
    }

    private static int startTracking(ServerCommandSource source, BlockPos min, BlockPos max) {
        SpawnTracker tracker = SpawnTracker.getOrCreateTracker(source, min, max);
        if (tracker.isActive()) {
            m(source, TRACKING_ACTIVE);
            return 1;
        }
        tracker.start();
        m(source, TRACKING_STARTED);
        return 1;
    }

    private static int stopTracking(ServerCommandSource source) {
        SpawnTracker tracker = SpawnTracker.getTracker(source);
        if (tracker == null) {
            m(source, TRACKING_INACTIVE);
            return 1;
        }
        tracker.stop();
        m(source, TRACKING_STOPPED);
        tracker.sendReport();
        return 1;
    }

    private static int sendMobcaps(ServerCommandSource source, ServerWorld dimension) {
        if (dimension == null) {
            dimension = source.getWorld();
        }
        var mobcaps = Mobcaps.getMobcaps(dimension);
        m(source, t(Keys.MOBCAPS_TITLE, dimension.getRegistryKey().getValue()));
        for (var e : mobcaps.entrySet()) {
            SpawnGroup category = e.getKey();
            Pair<Integer, Integer> pair = e.getValue();
            int cur = pair.getLeft();
            int max = pair.getRight();
            Formatting color = cur >= max ? Formatting.RED : (cur * 10 >= max * 8 ? Formatting.YELLOW : Formatting.GREEN);
            Text capText = cur + max == 0 ? s("-/-", Formatting.DARK_GREEN) : formats("%d/%d", color, cur, max);
            m(source, t(Keys.MOBCAPS_LINE, category, capText));
        }
        return 1;
    }

    private static int list(ServerCommandSource source) {
        return list(source, new BlockPos(source.getPosition()));
    }

    private static int list(ServerCommandSource source, BlockPos pos) {
        for (var text : SpawnUtils.list(source.getWorld(), pos)) {
            m(source, text);
        }
        return 1;
    }
}
