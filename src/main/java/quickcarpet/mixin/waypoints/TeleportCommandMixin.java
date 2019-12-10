package quickcarpet.mixin.waypoints;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TeleportCommand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import quickcarpet.annotation.Feature;
import quickcarpet.commands.WaypointCommand;
import quickcarpet.settings.Settings;
import quickcarpet.utils.Reflection;
import quickcarpet.utils.Waypoint;

import java.util.Collection;
import java.util.Collections;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.command.arguments.EntityArgumentType.entities;
import static net.minecraft.command.arguments.EntityArgumentType.getEntities;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static quickcarpet.utils.Messenger.*;

@Feature("waypoints")
@Mixin(TeleportCommand.class)
public abstract class TeleportCommandMixin {
    @SuppressWarnings("unchecked")
    @Redirect(method = "register", at = @At(
        value = "INVOKE",
        target = "Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;then(Lcom/mojang/brigadier/builder/ArgumentBuilder;)Lcom/mojang/brigadier/builder/ArgumentBuilder;",
        ordinal = 0),
        slice = @Slice(
            from = @At(value = "CONSTANT", args = "stringValue=destination", ordinal = 1),
            to = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;register(Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;)Lcom/mojang/brigadier/tree/LiteralCommandNode;", ordinal = 0)))
    private static <T extends ArgumentBuilder<ServerCommandSource, ?>> T waypointTeleport(LiteralArgumentBuilder<ServerCommandSource> node, ArgumentBuilder<ServerCommandSource, ?> argument) {
        node.then(literal("waypoint").requires(s -> s.hasPermissionLevel(Settings.commandWaypoint))
            .then(argument("waypoint", greedyString())
            .suggests(WaypointCommand::suggest)
            .executes(TeleportCommandMixin::teleportSourceToWaypoint)));
        node.then(argument("targets", entities())
            .then(literal("waypoint").requires(s -> s.hasPermissionLevel(Settings.commandWaypoint))
            .then(argument("waypoint", greedyString())
            .suggests(WaypointCommand::suggest)
            .executes(TeleportCommandMixin::teleportEntitiesToWaypoint))));
        return (T) node.then(argument);
    }

    private static int teleportSourceToWaypoint(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return teleportToWaypoint(ctx, Collections.singleton(ctx.getSource().getEntityOrThrow()));
    }

    private static int teleportEntitiesToWaypoint(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return teleportToWaypoint(ctx, getEntities(ctx, "targets"));
    }

    private static int teleportToWaypoint(CommandContext<ServerCommandSource> ctx, Collection<? extends Entity> entities) {
        ServerCommandSource source = ctx.getSource();
        String name = getString(ctx, "waypoint");
        Waypoint destination = WaypointCommand.getWaypoint(source, name);
        if (destination == null) {
            m(source, ts("command.waypoint.error.exists", RED, name));
            return -1;
        }
        ServerWorld world = source.getMinecraftServer().getWorld(destination.getDimension());
        Vec3d pos = destination.position;
        Vec2f rot = destination.rotation;
        for (Entity e : entities) {
            Reflection.teleport(source, e, world, pos.x, pos.y, pos.z, Collections.emptySet(), rot.y, rot.x);
        }
        if (entities.size() == 1) {
            send(source, t("commands.teleport.success.entity.single", entities.iterator().next().getDisplayName(), destination), true);
        } else {
            send(source, t("commands.teleport.success.entity.multiple", entities.size(), destination), true);
        }
        return entities.size();
    }
}
