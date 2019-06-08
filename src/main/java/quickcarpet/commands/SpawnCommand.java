package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.arguments.BlockPosArgumentType;
import net.minecraft.command.arguments.DimensionArgumentType;
import net.minecraft.entity.EntityCategory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import quickcarpet.helper.Mobcaps;
import quickcarpet.settings.Settings;
import quickcarpet.utils.Messenger;
import quickcarpet.utils.SpawnTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.minecraft.command.arguments.BlockPosArgumentType.getBlockPos;
import static net.minecraft.command.arguments.DimensionArgumentType.getDimensionArgument;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SpawnCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = literal("spawn")
            .requires((player) -> Settings.commandSpawn)
            .then(literal("mobcaps")
                .executes(c -> sendMobcaps(c.getSource(), null))
                .then(argument("dimension", DimensionArgumentType.create())
                    .executes(c -> sendMobcaps(c.getSource(), getDimensionArgument(c, "dimension")))))
            .then(literal("tracking")
                .executes(c -> sendTrackingReport(c.getSource()))
                .then(literal("start")
                    .executes(c -> startTracking(c.getSource(),null, null))
                    .then(argument("min", BlockPosArgumentType.create())
                    .then(argument("max", BlockPosArgumentType.create())
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
            Messenger.m(source, "d No tracker active");
            return 1;
        }
        tracker.sendReport();
        return 1;
    }

    private static int startTracking(ServerCommandSource source, BlockPos min, BlockPos max) throws CommandSyntaxException {
        SpawnTracker tracker = SpawnTracker.getOrCreateTracker(source.getPlayer(), min, max);
        if (tracker.isActive()) {
            Messenger.m(source, "d Tracking already active");
            return 1;
        }
        tracker.start();
        Messenger.m(source, "e Tracking started");
        return 1;
    }

    private static int stopTracking(ServerCommandSource source) throws CommandSyntaxException {
        SpawnTracker tracker = SpawnTracker.getTracker(source.getPlayer());
        if (tracker == null) {
            Messenger.m(source, "d No tracker active");
            return 1;
        }
        tracker.stop();
        Messenger.m(source, "e Tracking stopped");
        tracker.sendReport();
        return 1;
    }

    private static int sendMobcaps(ServerCommandSource source, DimensionType dimension) {
        if (dimension == null) dimension = source.getWorld().getDimension().getType();
        Map<EntityCategory, Pair<Integer, Integer>> mobcaps = Mobcaps.getMobcaps(dimension);
        List<Text> lst = new ArrayList<>();
        lst.add(Messenger.s(String.format("Mobcaps for %s:", Registry.DIMENSION.getId(dimension))));
        for (Map.Entry<EntityCategory, Pair<Integer, Integer>> e : mobcaps.entrySet()) {
            EntityCategory category = e.getKey();
            Pair<Integer, Integer> pair = e.getValue();
            int cur = pair.getLeft();
            int max = pair.getRight();
            lst.add(Messenger.c(String.format("w   %s: ", category),
                (cur+max==0)?"g -/-":String.format("%s %d/%d", (cur >= max)?"r":((cur >= 8*max/10)?"y":"l") ,cur, max)
            ));
        }
        Messenger.send(source, lst);
        return 1;
    }
}
