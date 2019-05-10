package quickcarpet.commands;

import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.arguments.DimensionArgumentType;
import net.minecraft.command.arguments.RotationArgumentType;
import net.minecraft.command.arguments.Vec3ArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.dimension.DimensionType;
import quickcarpet.patches.ServerPlayerEntityFake;
import quickcarpet.settings.Settings;
import quickcarpet.utils.Messenger;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PlayerCommand
{
public static void register(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        LiteralArgumentBuilder<ServerCommandSource> literalargumentbuilder = literal("player").
                requires((player) -> Settings.commandPlayer).
                then(argument("player", word()).
                        suggests( (c, b) -> CommandSource.suggestMatching(getPlayers(c.getSource()), b)).
                        /*
                        then(literal("stop").
                                executes(PlayerCommand::stop)).
                        then(literal("use").
                                executes(PlayerCommand::useOnce).
                                then(literal("once").
                                        executes( PlayerCommand::useOnce)).
                                then(literal("continuous").
                                        executes( PlayerCommand::useContinuous)).
                                then(literal("interval").
                                        then(argument("ticks",integer(2)).
                                                executes( PlayerCommand::useInterval)))).
                        then(literal("jump").
                                executes( PlayerCommand::jumpOnce).
                                then(literal("once").
                                        executes( PlayerCommand::jumpOnce)).
                                then(literal("continuous").
                                        executes( PlayerCommand::jumpContinuous)).
                                then(literal("interval").
                                        then(argument("ticks",integer(2)).
                                                executes( PlayerCommand::jumpInterval )))).
                        then(literal("attack").
                                executes(PlayerCommand::attackOnce).
                                then(literal("once").
                                        executes(PlayerCommand::attackOnce)).
                                then(literal("continuous").
                                        executes(PlayerCommand::attackContinuous)).
                                then(literal("interval").
                                        then(argument("ticks",integer(2)).
                                                executes(PlayerCommand::attackInterval)))).
                        then(literal("drop").
                                executes(PlayerCommand::dropItem)).
                        then(literal("swapHands").
                                executes(PlayerCommand::swapHands)).
                        */
                        then(literal("kill").
                                executes(PlayerCommand::kill)).
                        /*
                        then(literal("shadow").
                                executes(PlayerCommand::shadow)).
                        then(literal("mount").
                                executes(PlayerCommand::mount)).
                        then(literal("dismount").
                                executes(PlayerCommand::dismount)).
                        then(literal("sneak").
                                executes(PlayerCommand::sneak)).
                        then(literal("sprint").
                                executes(PlayerCommand::sprint)).
                        then(literal("look").
                                then(literal("north").
                                        executes(PlayerCommand::lookNorth)).
                                then(literal("south").
                                        executes(PlayerCommand::lookSouth)).
                                then(literal("east").
                                        executes(PlayerCommand::lookEast)).
                                then(literal("west").
                                        executes(PlayerCommand::lookWest)).
                                then(literal("up").
                                        executes(PlayerCommand::lookUp)).
                                then(literal("down").
                                        executes(PlayerCommand::lookDown)).
                                then(argument("direction",RotationArgument.rotation()).
                                        executes(PlayerCommand::lookAround))).
                        then(literal("turn").
                                then(literal("left").
                                        executes(PlayerCommand::turnLeft)).
                                then(literal("right").
                                        executes(PlayerCommand::turnRight)).
                                then(literal("back").
                                        executes(PlayerCommand::turnBack)).
                                then(argument("direction",RotationArgument.rotation()).
                                        executes(PlayerCommand::turn))).
                        then(literal("move").
                                then(literal("forward").
                                        executes(PlayerCommand::moveForward)).
                                then(literal("backward").
                                        executes(PlayerCommand::moveBackward)).
                                then(literal("left").
                                        executes(PlayerCommand::strafeLeft)).
                                then(literal("right").
                                        executes(PlayerCommand::strafeRight))).
                        */
                        then(literal("spawn").
                                executes(PlayerCommand::spawn).
                                then(literal("at").
                                        then(argument("position", Vec3ArgumentType.create()).
                                                executes(PlayerCommand::spawn).
                                                then(literal("facing").
                                                        then(argument("direction", RotationArgumentType.create()).
                                                                executes(PlayerCommand::spawn).
                                                                then(literal("in").
                                                                        then(argument("dimension", DimensionArgumentType.create()).
                                                                                executes(PlayerCommand::spawn)))))))));

        dispatcher.register(literalargumentbuilder);
    }
    
    private static Collection<String> getPlayers(ServerCommandSource source)
    {
        Set<String> players = Sets.newLinkedHashSet( Arrays.asList("Steve","Alex"));
        players.addAll(source.getPlayerNames());
        return players;
    }
    
    private static ServerPlayerEntity getPlayer(CommandContext<ServerCommandSource> context)
    {
        String playerName = getString(context,"player");
        MinecraftServer server = context.getSource().getMinecraftServer();
        return (ServerPlayerEntity) server.getPlayerManager().getPlayer(playerName);
    }
    
    private static boolean cantManipulate(CommandContext<ServerCommandSource> context)
    {
        PlayerEntity player = getPlayer(context);
        if (player == null)
        {
            Messenger.m(context.getSource(),"r Can only manipulate existing players");
            return true;
        }
        PlayerEntity sendingPlayer;
        try
        {
            sendingPlayer = context.getSource().getPlayer();
        }
        catch (CommandSyntaxException e)
        {
            return false;
        }
        
        if (!(context.getSource().getMinecraftServer().getPlayerManager().isOperator(sendingPlayer.getGameProfile())))
        {
            if (!(sendingPlayer == player || player instanceof ServerPlayerEntityFake))
            {
                Messenger.m(context.getSource(),"r Non OP players can't control other real players");
                return true;
            }
        }
        return false;
    }
    
    private static boolean cantReMove(CommandContext<ServerCommandSource> context)
    {
        if (cantManipulate(context))
            return true;
        PlayerEntity player = getPlayer(context);
        if (!(player instanceof ServerPlayerEntityFake))
        {
            Messenger.m(context.getSource(), "r Only fake players can be moved or killed");
            return true;
        }
        return false;
    }
    
    private static boolean cantSpawn(CommandContext<ServerCommandSource> context)
    {
        String playerName = getString(context,"player");
        PlayerEntity player = context.getSource().getMinecraftServer().getPlayerManager().getPlayer(playerName);
        if (player != null)
        {
            Messenger.m(context.getSource(), "r Player ", "rb "+playerName, "r is already logged on");
            return true;
        }
        return false;
    }
    
    private static int kill(CommandContext<ServerCommandSource> context)
    {
        if (cantReMove(context)) return 0;
        getPlayer(context).kill();
        return 1;
    }
    
    private static int spawn(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
    {
        if (cantSpawn(context)) return 0;
        Vec3d pos;
        Vec2f facing;
        DimensionType dim;
        try
        {
            pos = Vec3ArgumentType.getVec3(context, "position");
        }
        catch (IllegalArgumentException e)
        {
            pos = context.getSource().getPosition();
        }
        try
        {
            facing = RotationArgumentType.getRotation(context, "direction").toAbsoluteRotation(context.getSource());
        }
        catch (IllegalArgumentException e)
        {
            facing = context.getSource().getRotation();
        }
        try
        {
            dim = DimensionArgumentType.getDimensionArgument(context, "dimension");
        }
        catch (IllegalArgumentException e)
        {
            dim = context.getSource().getWorld().dimension.getType();
        }
        GameMode mode = GameMode.CREATIVE;
        try
        {
            ServerPlayerEntity player = context.getSource().getPlayer();
            mode = player.interactionManager.getGameMode();
        }
        catch (CommandSyntaxException ignored)
        {
        }
        PlayerEntity p = ServerPlayerEntityFake.createFake(
                getString(context,"player"),
                context.getSource().getMinecraftServer(),
                pos.x, pos.y, pos.z,
                facing.y, facing.x,
                dim,
                mode);
        if (p == null)
        {
            Messenger.m(context.getSource(), "rb Player "+getString(context,"player")+" doesn't exist " +
                                                     "and cannot spawn in online mode. Turn the server offline to spawn non-existing players");
        }
        return 1;
    }
}
