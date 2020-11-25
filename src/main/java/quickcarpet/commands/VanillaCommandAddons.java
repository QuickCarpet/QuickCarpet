package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import quickcarpet.mixin.accessor.TeleportCommandAccessor;
import quickcarpet.settings.Settings;
import quickcarpet.utils.Waypoint;

import java.util.Collection;
import java.util.Collections;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.command.argument.EntityArgumentType.entities;
import static net.minecraft.command.argument.EntityArgumentType.getEntities;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static quickcarpet.utils.Messenger.*;

public class VanillaCommandAddons {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> tp = literal("teleport")
            .then(literal("waypoint")
                .requires(s -> s.hasPermissionLevel(Settings.commandWaypoint))
                .then(argument("waypoint", greedyString())
                    .suggests(WaypointCommand::suggest)
                    .executes(VanillaCommandAddons::teleportSourceToWaypoint)))
            .then(argument("targets", entities())
                .then(literal("waypoint").requires(s -> s.hasPermissionLevel(Settings.commandWaypoint))
                    .then(argument("waypoint", greedyString())
                        .suggests(WaypointCommand::suggest)
                        .executes(VanillaCommandAddons::teleportEntitiesToWaypoint))));
        dispatcher.register(tp);
    }

    private static int teleportSourceToWaypoint(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return teleportToWaypoint(ctx, Collections.singleton(ctx.getSource().getEntityOrThrow()));
    }

    private static int teleportEntitiesToWaypoint(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return teleportToWaypoint(ctx, getEntities(ctx, "targets"));
    }

    private static int teleportToWaypoint(CommandContext<ServerCommandSource> ctx, Collection<? extends Entity> entities) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        String name = getString(ctx, "waypoint");
        Waypoint destination = WaypointCommand.getWaypoint(source, name);
        if (destination == null) {
            m(source, ts("command.waypoint.error.notFound", Formatting.RED, name));
            return -1;
        }
        ServerWorld world = source.getMinecraftServer().getWorld(destination.getDimension());
        Vec3d pos = destination.position;
        Vec2f rot = destination.rotation;
        for (Entity e : entities) {
            TeleportCommandAccessor.invokeTeleport(source, e, world, pos.x, pos.y, pos.z, Collections.emptySet(), rot.y, rot.x, null);
        }
        if (entities.size() == 1) {
            send(source, t("commands.teleport.success.entity.single", entities.iterator().next().getDisplayName(), destination), true);
        } else {
            send(source, t("commands.teleport.success.entity.multiple", entities.size(), destination), true);
        }
        return entities.size();
    }
}
