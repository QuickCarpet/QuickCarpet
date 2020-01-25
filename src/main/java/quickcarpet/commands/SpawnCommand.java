package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.EntityCategory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import quickcarpet.helper.Mobcaps;
import quickcarpet.settings.Settings;
import quickcarpet.utils.SpawnTracker;

import java.util.Map;

import static net.minecraft.command.arguments.BlockPosArgumentType.blockPos;
import static net.minecraft.command.arguments.BlockPosArgumentType.getBlockPos;
import static net.minecraft.command.arguments.DimensionArgumentType.dimension;
import static net.minecraft.command.arguments.DimensionArgumentType.getDimensionArgument;
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

    private static int sendTrackingReport(ServerCommandSource source) throws CommandSyntaxException {
        SpawnTracker tracker = SpawnTracker.getTracker(source.getPlayer());
        if (tracker == null) {
            m(source, ts("command.spawn.tracking.inactive", GOLD));
            return 1;
        }
        tracker.sendReport();
        return 1;
    }

    private static int startTracking(ServerCommandSource source, BlockPos min, BlockPos max) throws CommandSyntaxException {
        SpawnTracker tracker = SpawnTracker.getOrCreateTracker(source.getPlayer(), min, max);
        if (tracker.isActive()) {
            m(source, ts("command.spawn.tracking.active", GOLD));
            return 1;
        }
        tracker.start();
        m(source, ts("command.spawn.tracking.started", DARK_GREEN));
        return 1;
    }

    private static int stopTracking(ServerCommandSource source) throws CommandSyntaxException {
        SpawnTracker tracker = SpawnTracker.getTracker(source.getPlayer());
        if (tracker == null) {
            m(source, ts("command.spawn.tracking.active", GOLD));
            return 1;
        }
        tracker.stop();
        m(source, ts("command.spawn.tracking.stopped", DARK_GREEN));
        tracker.sendReport();
        return 1;
    }

    private static int sendMobcaps(ServerCommandSource source, DimensionType dimension) {
        if (dimension == null) dimension = source.getWorld().getDimension().getType();
        Map<EntityCategory, Pair<Integer, Integer>> mobcaps = Mobcaps.getMobcaps(dimension);
        m(source, t("command.spawn.mobcaps.title", Registry.DIMENSION_TYPE.getId(dimension)));
        for (Map.Entry<EntityCategory, Pair<Integer, Integer>> e : mobcaps.entrySet()) {
            EntityCategory category = e.getKey();
            Pair<Integer, Integer> pair = e.getValue();
            int cur = pair.getLeft();
            int max = pair.getRight();
            char color = cur >= max ? RED : (cur * 10 >= max * 8 ? YELLOW : LIME);
            Text capText = cur + max == 0 ? s("-/-", DARK_GREEN) : formats("%d/%d", color, cur, max);
            m(source, t("command.spawn.mobcaps.line", category, capText));
        }
        return 1;
    }
}
