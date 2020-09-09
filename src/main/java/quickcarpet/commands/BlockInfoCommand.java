package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.block.BlockState;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import quickcarpet.helper.BlockInfoProvider;
import quickcarpet.settings.Settings;

import static net.minecraft.command.argument.BlockPosArgumentType.getLoadedBlockPos;
import static net.minecraft.server.command.CommandManager.literal;
import static quickcarpet.utils.Messenger.*;

public class BlockInfoCommand {
    public static final DynamicCommandExceptionType UNKNOWN_PROVIDER_EXCEPTION = new DynamicCommandExceptionType(
        id -> new TranslatableText("block_info_provider.unknown", id)
    );

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> blockInfo = Utils.makeStateInfoCommand(
            literal("blockinfo"),
            BlockInfoProvider.REGISTRY,
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
        m(source, t("command.stateinfo.line", t("command.stateinfo.block_state"), format(state)));
        return Utils.executeStateInfo(source, pos, state, BlockInfoProvider.REGISTRY);
    }

    private static int executeDirection(CommandContext<ServerCommandSource> ctx, Direction direction) throws CommandSyntaxException {
        return Utils.executeStateInfo(ctx, direction, BlockInfoProvider.REGISTRY, BlockView::getBlockState, UNKNOWN_PROVIDER_EXCEPTION::create);
    }
}
