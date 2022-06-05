package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.block.BlockState;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import quickcarpet.settings.Settings;
import quickcarpet.utils.Constants.StateInfoCommand.Keys;
import quickcarpet.utils.QuickCarpetRegistries;

import static net.minecraft.command.argument.BlockPosArgumentType.getLoadedBlockPos;
import static net.minecraft.server.command.CommandManager.literal;
import static quickcarpet.utils.Constants.StateInfoCommand.Texts.BLOCK_STATE;
import static quickcarpet.utils.Messenger.*;

public class BlockInfoCommand {
    public static final DynamicCommandExceptionType UNKNOWN_PROVIDER_EXCEPTION = new DynamicCommandExceptionType(
        id -> t("block_info_provider.unknown", id)
    );

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var blockInfo = Utils.makeStateInfoCommand(
            literal("blockinfo"),
            QuickCarpetRegistries.BLOCK_INFO_PROVIDER,
            BlockInfoCommand::execute,
            BlockInfoCommand::executeDirection
        ).requires(source -> source.hasPermissionLevel(Settings.commandBlockInfo));

        dispatcher.register(blockInfo);
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        ServerWorld world = source.getWorld();
        BlockPos pos = getLoadedBlockPos(ctx, "pos");
        BlockState state = world.getBlockState(pos);
        m(source, t(Keys.LINE, BLOCK_STATE, format(state)));
        return Utils.executeStateInfo(source, pos, state, QuickCarpetRegistries.BLOCK_INFO_PROVIDER);
    }

    private static int executeDirection(CommandContext<ServerCommandSource> ctx, Direction direction) throws CommandSyntaxException {
        return Utils.executeStateInfo(ctx, direction, QuickCarpetRegistries.BLOCK_INFO_PROVIDER, BlockView::getBlockState, UNKNOWN_PROVIDER_EXCEPTION::create);
    }
}
