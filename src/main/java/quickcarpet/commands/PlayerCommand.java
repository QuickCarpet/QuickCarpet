package quickcarpet.commands;

import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.arguments.DimensionArgumentType;
import net.minecraft.command.arguments.RotationArgumentType;
import net.minecraft.command.arguments.Vec3ArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.dimension.DimensionType;
import quickcarpet.helper.PlayerActionPack;
import quickcarpet.helper.PlayerActionPack.Action;
import quickcarpet.helper.PlayerActionPack.ActionType;
import quickcarpet.patches.FakeServerPlayerEntity;
import quickcarpet.settings.Settings;
import quickcarpet.utils.extensions.ActionPackOwner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.command.arguments.DimensionArgumentType.dimension;
import static net.minecraft.command.arguments.RotationArgumentType.getRotation;
import static net.minecraft.command.arguments.RotationArgumentType.rotation;
import static net.minecraft.command.arguments.Vec3ArgumentType.vec3;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static quickcarpet.utils.Messenger.*;

public class PlayerCommand {
    // TODO: allow any order like execute
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> literalargumentbuilder = literal("player")
            .requires(s -> s.hasPermissionLevel(Settings.commandPlayer))
            .then(argument("player", word())
                .suggests( (c, b) -> CommandSource.suggestMatching(getPlayers(c.getSource()), b))
                .then(literal("stop").executes(PlayerCommand::stop))
                .then(makeActionCommand("use", ActionType.USE))
                .then(makeActionCommand("jump", ActionType.JUMP))
                .then(makeActionCommand("attack", ActionType.ATTACK))
                .then(makeActionCommand("drop", ActionType.DROP_ITEM))
                .then(makeActionCommand("dropStack", ActionType.DROP_STACK))
                .then(makeActionCommand("swapHands", ActionType.SWAP_HANDS))
                .then(literal("kill").executes(PlayerCommand::kill))
                .then(literal("shadow"). executes(PlayerCommand::shadow))
                .then(literal("mount").executes(manipulation(PlayerActionPack::mount)))
                .then(literal("dismount").executes(manipulation(PlayerActionPack::dismount)))
                .then(literal("sneak").executes(manipulation(PlayerActionPack::toggleSneaking)))
                .then(literal("sprint").executes(manipulation(PlayerActionPack::toggleSprinting)))
                .then(literal("look")
                    .then(literal("north").executes(manipulation(ap -> ap.look(Direction.NORTH))))
                    .then(literal("south").executes(manipulation(ap -> ap.look(Direction.SOUTH))))
                    .then(literal("east").executes(manipulation(ap -> ap.look(Direction.EAST))))
                    .then(literal("west").executes(manipulation(ap -> ap.look(Direction.WEST))))
                    .then(literal("up").executes(manipulation(ap -> ap.look(Direction.UP))))
                    .then(literal("down").executes(manipulation(ap -> ap.look(Direction.DOWN))))
                    .then(argument("direction", rotation())
                        .executes(c -> manipulate(c, ap -> ap.look(getRotation(c, "direction").toAbsoluteRotation(c.getSource())))))
                ).then(literal("turn")
                    .then(literal("left").executes(c -> manipulate(c, ap -> ap.turn(-90, 0))))
                    .then(literal("right").executes(c -> manipulate(c, ap -> ap.turn(90, 0))))
                    .then(literal("back").executes(c -> manipulate(c, ap -> ap.turn(180, 0))))
                    .then(argument("rotation", rotation())
                        .executes(c -> manipulate(c, ap -> ap.turn(getRotation(c, "rotation").toAbsoluteRotation(c.getSource())))))
                ).then(literal("move")
                    .then(literal("forward").executes(c -> manipulate(c, ap -> ap.setForward(1))))
                    .then(literal("backward").executes(c -> manipulate(c, ap -> ap.setForward(-1))))
                    .then(literal("left").executes(c -> manipulate(c, ap -> ap.setSideways(1))))
                    .then(literal("right").executes(c -> manipulate(c, ap -> ap.setSideways(-1))))
                ).then(literal("spawn").executes(PlayerCommand::spawn)
                    .then(literal("at").then(argument("position", vec3()).executes(PlayerCommand::spawn)
                        .then(literal("facing").then(argument("direction", rotation()).executes(PlayerCommand::spawn)
                            .then(literal("in").then(argument("dimension", dimension()).executes(PlayerCommand::spawn)
                                .then(literal("as")
                                    .then(literal("spectator").requires(s -> s.hasPermissionLevel(Settings.commandCameramode) || s.hasPermissionLevel(2)).executes(ctx -> spawn(ctx, GameMode.SPECTATOR)))
                                    .then(literal("creative").requires(s -> s.hasPermissionLevel(2)).executes(ctx -> spawn(ctx, GameMode.CREATIVE)))
                                    .then(literal("survival").executes(ctx -> spawn(ctx, GameMode.SURVIVAL)))
                                    .then(literal("adventure").executes(ctx -> spawn(ctx, GameMode.ADVENTURE)))
            )))))))));
        dispatcher.register(literalargumentbuilder);
    }

    private static LiteralArgumentBuilder<ServerCommandSource> makeActionCommand(String actionName, ActionType type) {
        return literal(actionName)
            .executes(c -> action(c, type, Action.once()))
            .then(literal("once").executes(c -> action(c, type, Action.once())))
            .then(literal("continuous").executes(c -> action(c, type, Action.continuous())))
            .then(literal("interval").then(argument("ticks", integer(2))
                .executes(c -> action(c, type, Action.interval(getInteger(c, "ticks"))))));
    }

    private static Collection<String> getPlayers(ServerCommandSource source) {
        Set<String> players = Sets.newLinkedHashSet(Arrays.asList("Steve", "Alex"));
        players.addAll(source.getPlayerNames());
        return players;
    }

    private static ServerPlayerEntity getPlayer(CommandContext<ServerCommandSource> context) {
        String playerName = getString(context, "player");
        MinecraftServer server = context.getSource().getMinecraftServer();
        return server.getPlayerManager().getPlayer(playerName);
    }

    private static boolean cantManipulate(CommandContext<ServerCommandSource> context) {
        PlayerEntity player = getPlayer(context);
        if (player == null) {
            m(context.getSource(), ts("command.player.onlyExisting", RED));
            return true;
        }
        PlayerEntity sendingPlayer;
        try {
            sendingPlayer = context.getSource().getPlayer();
        } catch (CommandSyntaxException e) {
            return false;
        }

        if (!context.getSource().getMinecraftServer().getPlayerManager().isOperator(sendingPlayer.getGameProfile())) {
            if (sendingPlayer != player && !(player instanceof FakeServerPlayerEntity)) {
                m(context.getSource(), ts("command.player.notOperator", RED));
                return true;
            }
        }
        return false;
    }

    private static boolean cantReMove(CommandContext<ServerCommandSource> context) {
        if (cantManipulate(context)) return true;
        PlayerEntity player = getPlayer(context);
        if (player instanceof FakeServerPlayerEntity) return false;
        m(context.getSource(), ts("command.player.notFake", RED));
        return true;
    }

    private static boolean cantSpawn(CommandContext<ServerCommandSource> context) {
        String playerName = getString(context, "player");
        MinecraftServer server = context.getSource().getMinecraftServer();
        PlayerManager manager = server.getPlayerManager();
        PlayerEntity player = manager.getPlayer(playerName);
        if (player != null) {
            m(context.getSource(), ts("command.player.alreadyOnline", RED, s(playerName, BOLD)));
            return true;
        }
        GameProfile profile = server.getUserCache().findByName(playerName);
        if (manager.getUserBanList().contains(profile)) {
            m(context.getSource(), ts("command.player.banned", RED, s(playerName, BOLD)));
            return true;
        }
        if (manager.isWhitelistEnabled() && profile != null && manager.isWhitelisted(profile) && !context.getSource().hasPermissionLevel(2)) {
            m(context.getSource(), "command.player.whitelisted", RED);
            return true;
        }
        return false;
    }

    private static int kill(CommandContext<ServerCommandSource> context) {
        if (cantReMove(context)) return 0;
        getPlayer(context).kill();
        return 1;
    }

    @FunctionalInterface
    interface SupplierWithCommandSyntaxException<T> {
        T get() throws CommandSyntaxException;
    }

    private static <T> T tryGetArg(SupplierWithCommandSyntaxException<T> a, SupplierWithCommandSyntaxException<T> b) throws CommandSyntaxException {
        try {
            return a.get();
        } catch (IllegalArgumentException e) {
            return b.get();
        }
    }

    private static int spawn(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return spawn(context, GameMode.NOT_SET);
    }

    private static int spawn(CommandContext<ServerCommandSource> context, GameMode gameMode) throws CommandSyntaxException {
        if (cantSpawn(context)) return 0;
        ServerCommandSource source = context.getSource();
        Vec3d pos = tryGetArg(
                () -> Vec3ArgumentType.getVec3(context, "position"),
                source::getPosition);
        Vec2f facing = tryGetArg(
                () -> RotationArgumentType.getRotation(context, "direction").toAbsoluteRotation(context.getSource()),
                source::getRotation);
        DimensionType dim = tryGetArg(
                () -> DimensionArgumentType.getDimensionArgument(context, "dimension"),
                () -> source.getWorld().dimension.getType());
        GameMode mode = GameMode.CREATIVE;
        try {
            ServerPlayerEntity player = context.getSource().getPlayer();
            mode = player.interactionManager.getGameMode();
        } catch (CommandSyntaxException ignored) {}
        if (gameMode != GameMode.NOT_SET) mode = gameMode;
        String playerName = getString(context, "player");
        MinecraftServer server = source.getMinecraftServer();
        PlayerEntity player = FakeServerPlayerEntity.createFake(playerName, server, pos.x, pos.y, pos.z, facing.y, facing.x, dim, mode);
        if (player == null) {
            m(context.getSource(), ts("command.player.doesNotExist", RED, s(playerName, BOLD)));
        }
        return 1;
    }

    private static int stop(CommandContext<ServerCommandSource> context) {
        if (cantManipulate(context)) return 0;
        ServerPlayerEntity player = getPlayer(context);
        ((ActionPackOwner) player).getActionPack().stop();
        return 1;
    }

    private static int manipulate(CommandContext<ServerCommandSource> context, Consumer<PlayerActionPack> action) {
        if (cantManipulate(context)) return 0;
        ServerPlayerEntity player = getPlayer(context);
        action.accept(((ActionPackOwner) player).getActionPack());
        return 1;
    }

    private static Command<ServerCommandSource> manipulation(Consumer<PlayerActionPack> action) {
        return c -> manipulate(c, action);
    }

    private static int action(CommandContext<ServerCommandSource> context, ActionType type, Action action) {
        return manipulate(context, ap -> ap.start(type, action));
    }

    private static int shadow(CommandContext<ServerCommandSource> context) {
        if (cantManipulate(context)) return 0;
        ServerPlayerEntity player = getPlayer(context);
        if (player instanceof FakeServerPlayerEntity) {
            m(context.getSource(), ts("command.player.shadowFake", RED));
            return 0;
        }
        FakeServerPlayerEntity.createShadow(player.server, player);
        return 1;
    }
}
