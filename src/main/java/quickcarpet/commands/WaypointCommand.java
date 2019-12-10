package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import quickcarpet.QuickCarpet;
import quickcarpet.settings.Settings;
import quickcarpet.utils.Waypoint;
import quickcarpet.utils.extensions.WaypointContainer;

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
import static net.minecraft.command.arguments.DimensionArgumentType.dimension;
import static net.minecraft.command.arguments.DimensionArgumentType.getDimensionArgument;
import static net.minecraft.command.arguments.RotationArgumentType.rotation;
import static net.minecraft.command.arguments.Vec3ArgumentType.vec3;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static quickcarpet.utils.Messenger.*;

public class WaypointCommand {
    private static final SimpleCommandExceptionType INVALID_PAGE = new SimpleCommandExceptionType(t("command.waypoint.list.invalidPage"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> waypoint = literal("waypoint")
            .requires(s -> s.hasPermissionLevel(Settings.commandWaypoint));
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

    public static CompletableFuture<Suggestions> suggest(CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
        ServerCommandSource source = ctx.getSource();
        @SuppressWarnings("unchecked")
        Stream<String> waypointNames = Waypoint
            .getAllWaypoints((Iterable<WaypointContainer>) (Iterable) source.getMinecraftServer().getWorlds())
            .stream().filter(w -> w.canManipulate(source))
            .flatMap(w -> Stream.of(w.name, w.getFullName()));
        return CommandSource.suggestMatching(waypointNames, builder);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static Waypoint getWaypoint(ServerCommandSource source, String name) {
        return Waypoint.find(name, (WaypointContainer) source.getWorld(),
                (Iterable<WaypointContainer>) (Iterable) source.getMinecraftServer().getWorlds());
    }

    private static int add(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        String name = getString(ctx, "name");
        Vec3d pos = Utils.getOrDefault(ctx, "position", source.getPosition());
        DimensionType dim = Utils.getOrDefault(ctx, "dimension", source.getWorld().getDimension().getType());
        Vec2f rot = Utils.getOrDefault(ctx, "rotation", source.getRotation());
        WaypointContainer world = (WaypointContainer) QuickCarpet.minecraft_server.getWorld(dim);
        Map<String, Waypoint> waypoints = world.getWaypoints();
        if (waypoints.containsKey(name)) {
            m(source, ts("command.waypoint.error.exists", RED, name));
            return -1;
        }
        Waypoint w = new Waypoint(world, name, source.getPlayer(), pos, rot);
        world.getWaypoints().put(name, w);
        m(source, t("command.waypoint.added", w, tp("c", w)));
        return 0;
    }

    private static int remove(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        String name = getString(ctx, "name");
        Waypoint w = getWaypoint(source, name);
        if (w == null) {
            m(source, ts("command.waypoint.error.notFound", RED, name));
            return -1;
        }
        if (!w.canManipulate(source)) {
            m(source, ts("command.waypoint.remove.notAllowed", RED, w));
            return -2;
        }
        if (w.world.getWaypoints().remove(w.name, w)) {
            m(source, t("command.waypoint.remove.success", w));
            return 1;
        }
        return 0;
    }

    private static int listAll(ServerCommandSource source, int page) throws CommandSyntaxException {
        ArrayList<Waypoint> waypoints = new ArrayList<>();
        for (ServerWorld world : QuickCarpet.minecraft_server.getWorlds()) {
            waypoints.addAll(((WaypointContainer) world).getWaypoints().values());
        }
        return printList(source, waypoints, page, null, null);
    }

    private static int listCreator(ServerCommandSource source, String creator, int page) throws CommandSyntaxException {
        ArrayList<Waypoint> waypoints = new ArrayList<>();
        for (ServerWorld world : QuickCarpet.minecraft_server.getWorlds()) {
            for (Waypoint w : ((WaypointContainer) world).getWaypoints().values()) {
                if (creator.equalsIgnoreCase(w.creator)) waypoints.add(w);
            }
        }
        return printList(source, waypoints, page, null, creator);
    }

    private static int listDimension(ServerCommandSource source, DimensionType dimensionType, int page) throws CommandSyntaxException {
        Collection<Waypoint> waypoints = ((WaypointContainer) QuickCarpet.minecraft_server.getWorld(dimensionType)).getWaypoints().values();
        return printList(source, waypoints, page, dimensionType, null);
    }

    private static int printList(ServerCommandSource source, Collection<Waypoint> waypoints, int page, @Nullable DimensionType dimensionType, @Nullable String creator) throws CommandSyntaxException {
        if (waypoints.isEmpty()) {
            m(source, ts("command.waypoint.list.none", GOLD));
            return 0;
        }
        int PAGE_SIZE = 20;
        int pages = (waypoints.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        if (page > pages) throw INVALID_PAGE.create();
        Text header;
        if (dimensionType != null) {
            header = t("command.waypoint.list.header.dimension", s(dimensionType.toString(), DARK_GREEN));
        } else if (creator != null) {
            header = t("command.waypoint.list.header.creator", s(creator, DARK_GREEN));
        } else {
            header = t("command.waypoint.list.header.all");
        }
        if (pages > 1) {
            header.append(" ").append(t("command.waypoint.list.page", page, pages));
            String baseCommand = "/waypoint list";
            if (dimensionType != null) baseCommand += " in " + dimensionType;
            else if (creator != null) baseCommand += " by " + creator;
            if (page > 1) {
                header.append(" ").append(runCommand(s("[<]", GRAY), baseCommand + " " + (page - 1),
                        t("command.waypoint.list.page.previous")));
            }
            if (page < pages) {
                header.append(" ").append(runCommand(s("[>]", GRAY), baseCommand + " " + (page + 1),
                        t("command.waypoint.list.page.next")));
            }
        }
        header.append(s(":", GRAY));
        m(source, header);
        int from = (page - 1) * PAGE_SIZE, to = Math.min(page * PAGE_SIZE, waypoints.size());
        Waypoint[] pageWaypoints = Arrays.copyOfRange(waypoints.toArray(new Waypoint[0]), from, to);
        for (Waypoint w : pageWaypoints) {
            if (dimensionType == null) {
                if (creator == null && w.creator != null) {
                    m(source, t("command.waypoint.list.entry.creator",
                        w, tp("c", w), s(w.creator, DARK_GREEN)));
                } else {
                    m(source, t("command.waypoint.list.entry",
                        w, tp("c", w)));
                }
            } else {
                if (creator == null && w.creator != null) {
                    m(source, t("command.waypoint.list.entry.creator", s(w.name, YELLOW), tp("c", w), s(w.creator, DARK_GREEN)));
                } else {
                    m(source, t("command.waypoint.list.entry", s(w.name, YELLOW), tp("c", w)));
                }
            }
        }
        return waypoints.size();
    }
}
