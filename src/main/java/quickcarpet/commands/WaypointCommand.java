package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.DefaultPosArgument;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import quickcarpet.settings.Settings;
import quickcarpet.utils.Constants.WaypointCommand.Keys;
import quickcarpet.utils.Waypoint;
import quickcarpet.utils.mixin.extensions.WaypointContainer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.command.argument.DimensionArgumentType.dimension;
import static net.minecraft.command.argument.DimensionArgumentType.getDimensionArgument;
import static net.minecraft.command.argument.RotationArgumentType.rotation;
import static net.minecraft.command.argument.Vec3ArgumentType.vec3;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static quickcarpet.utils.Constants.WaypointCommand.Texts.*;
import static quickcarpet.utils.Messenger.*;

public class WaypointCommand {
    private static final SimpleCommandExceptionType INVALID_PAGE = new SimpleCommandExceptionType(LIST_INVALID_PAGE);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var waypoint = literal("waypoint").requires(s -> s.hasPermissionLevel(Settings.commandWaypoint));
        waypoint.then(literal("add")
            .then(argument("name", string()).executes(WaypointCommand::add)
            .then(argument("position", vec3()).executes(WaypointCommand::add)
            .then(argument("dimension", dimension()).executes(WaypointCommand::add)
            .then(argument("rotation", rotation()).executes(WaypointCommand::add)
        )))));
        waypoint.then(literal("list")
            .executes(c -> listAll(c.getSource(), 1))
            .then(argument("page", integer(1)).executes(c -> listAll(c.getSource(), getInteger(c, "page"))))
            .then(literal("in").then(argument("dimension", dimension())
                .executes(c -> listDimension(c.getSource(), getDimensionArgument(c, "dimension"), 1))
                .then(argument("page", integer(1))
                    .executes(c -> listDimension(c.getSource(), getDimensionArgument(c, "dimension"), getInteger(c, "page"))))
            )).then(literal("by").then(argument("creator", string())
                .executes(c -> listCreator(c.getSource(), getString(c, "creator"), 1))
                .then(argument("page", integer(1))
                    .executes(c -> listCreator(c.getSource(), getString(c, "creator"), getInteger(c, "page"))))
            ))
        );
        waypoint.then(literal("remove")
            .then(argument("name", string()).suggests(WaypointCommand::suggest).executes(WaypointCommand::remove))
        );
        dispatcher.register(waypoint);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Iterable<WaypointContainer> toWaypointContainers(Iterable<ServerWorld> worlds) {
        return (Iterable) worlds;
    }

    public static CompletableFuture<Suggestions> suggest(CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
        ServerCommandSource source = ctx.getSource();
        Stream<String> waypointNames = Waypoint
            .getAllWaypoints(toWaypointContainers(source.getServer().getWorlds()))
            .stream().filter(w -> w.canManipulate(source))
            .flatMap(w -> Stream.of(w.name(), w.getFullName()));
        return CommandSource.suggestMatching(waypointNames, builder);
    }

    @Nullable
    public static Waypoint getWaypoint(ServerCommandSource source, String name) {
        return Waypoint.find(name, (WaypointContainer) source.getWorld(), toWaypointContainers(source.getServer().getWorlds()));
    }

    private static int add(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        String name = getString(ctx, "name");
        Vec3d pos = Utils.getOrDefault(ctx, "position", DefaultPosArgument.zero()).toAbsolutePos(source);
        ServerWorld dim = Utils.getOrDefault(ctx, "dimension", DimensionArgumentType::getDimensionArgument, source.getWorld());
        Vec2f rot = Utils.getOrDefault(ctx, "rotation", DefaultPosArgument.zero()).toAbsoluteRotation(source);
        WaypointContainer world = (WaypointContainer) dim;
        Map<String, Waypoint> waypoints = world.quickcarpet$getWaypoints();
        if (waypoints.containsKey(name)) {
            m(source, ts(Keys.ERROR_EXISTS, Formatting.RED, name));
            return -1;
        }
        Waypoint w = new Waypoint(world, name, source.getPlayer(), pos, rot);
        world.quickcarpet$getWaypoints().put(name, w);
        m(source, t(Keys.ADDED, w, tp(w, Formatting.AQUA)));
        return 0;
    }

    private static int remove(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        String name = getString(ctx, "name");
        Waypoint w = getWaypoint(source, name);
        if (w == null) {
            m(source, ts(Keys.ERROR_NOT_FOUND, Formatting.RED, name));
            return -1;
        }
        if (!w.canManipulate(source)) {
            m(source, ts(Keys.REMOVE_NOT_ALLOWED, Formatting.RED, w));
            return -2;
        }
        if (w.world().quickcarpet$getWaypoints().remove(w.name(), w)) {
            m(source, t(Keys.REMOVE_SUCCESS, w));
            return 1;
        }
        return 0;
    }

    private static int listAll(ServerCommandSource source, int page) throws CommandSyntaxException {
        ArrayList<Waypoint> waypoints = new ArrayList<>();
        for (ServerWorld world : source.getServer().getWorlds()) {
            waypoints.addAll(((WaypointContainer) world).quickcarpet$getWaypoints().values());
        }
        return printList(source, waypoints, page, null, null);
    }

    private static int listCreator(ServerCommandSource source, String creator, int page) throws CommandSyntaxException {
        ArrayList<Waypoint> waypoints = new ArrayList<>();
        for (ServerWorld world : source.getServer().getWorlds()) {
            for (Waypoint w : ((WaypointContainer) world).quickcarpet$getWaypoints().values()) {
                if (creator.equalsIgnoreCase(w.creator())) waypoints.add(w);
            }
        }
        return printList(source, waypoints, page, null, creator);
    }

    private static int listDimension(ServerCommandSource source, ServerWorld dimension, int page) throws CommandSyntaxException {
        Collection<Waypoint> waypoints = ((WaypointContainer) dimension).quickcarpet$getWaypoints().values();
        return printList(source, waypoints, page, dimension, null);
    }

    private static int printList(ServerCommandSource source, Collection<Waypoint> waypoints, int page, @Nullable ServerWorld dimension, @Nullable String creator) throws CommandSyntaxException {
        if (waypoints.isEmpty()) {
            m(source, LIST_NONE);
            return 0;
        }
        int PAGE_SIZE = 20;
        int pages = (waypoints.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        if (page > pages) throw INVALID_PAGE.create();
        MutableText header;
        if (dimension != null) {
            header = t(Keys.LIST_HEADER_DIMENSION, s(dimension.toString(), Formatting.DARK_GREEN));
        } else if (creator != null) {
            header = t(Keys.LIST_HEADER_CREATOR, s(creator, Formatting.DARK_GREEN));
        } else {
            header = LIST_HEADER_ALL.copy();
        }
        if (pages > 1) {
            header.append(" ").append(t(Keys.LIST_PAGE, page, pages));
            String baseCommand = "/waypoint list";
            if (dimension != null) baseCommand += " in " + dimension.getRegistryKey().getValue();
            else if (creator != null) baseCommand += " by " + creator;
            if (page > 1) {
                header.append(" ").append(runCommand(s("[<]", Formatting.GRAY), baseCommand + " " + (page - 1), LIST_PAGE_PREVIOUS));
            }
            if (page < pages) {
                header.append(" ").append(runCommand(s("[>]", Formatting.GRAY), baseCommand + " " + (page + 1), LIST_PAGE_NEXT));
            }
        }
        header.append(s(":", Formatting.GRAY));
        m(source, header);
        int from = (page - 1) * PAGE_SIZE, to = Math.min(page * PAGE_SIZE, waypoints.size());
        Waypoint[] pageWaypoints = Arrays.copyOfRange(waypoints.toArray(new Waypoint[0]), from, to);
        for (Waypoint w : pageWaypoints) {
            if (dimension == null) {
                if (creator == null && w.creator() != null) {
                    m(source, t(Keys.LIST_ENTRY_CREATOR, w, tp(w, Formatting.AQUA), s(w.creator(), Formatting.DARK_GREEN)));
                } else {
                    m(source, t(Keys.LIST_ENTRY, w, tp(w, Formatting.AQUA)));
                }
            } else {
                if (creator == null && w.creator() != null) {
                    m(source, t(Keys.LIST_ENTRY_CREATOR, s(w.name(), Formatting.YELLOW), tp(w, Formatting.AQUA), s(w.creator(), Formatting.DARK_GREEN)));
                } else {
                    m(source, t(Keys.LIST_ENTRY, s(w.name(), Formatting.YELLOW), tp(w, Formatting.AQUA)));
                }
            }
        }
        return waypoints.size();
    }
}
