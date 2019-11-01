package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockPos;
import quickcarpet.settings.Settings;

import java.util.function.Predicate;

import static net.minecraft.command.arguments.BlockPosArgumentType.blockPos;
import static net.minecraft.command.arguments.BlockPosArgumentType.getLoadedBlockPos;
import static net.minecraft.command.arguments.BlockStateArgumentType.blockState;
import static net.minecraft.command.arguments.BlockStateArgumentType.getBlockState;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static quickcarpet.utils.Constants.SetBlockState.NO_FILL_UPDATE;
import static quickcarpet.utils.Constants.SetBlockState.SEND_TO_CLIENT;

public class CarpetSetBlockCommand {
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.setblock.failed"));

    public static void register(CommandDispatcher<ServerCommandSource> commandDispatcher_1) {
        LiteralArgumentBuilder<ServerCommandSource> carpetsetblock = literal("carpetsetblock")
            .requires(s -> s.hasPermissionLevel(Settings.commandCarpetSetBlock))
            .then(argument("pos", blockPos())
            .then((argument("block", blockState())
                .executes((commandContext_1) -> execute(commandContext_1.getSource(), getLoadedBlockPos(commandContext_1, "pos"), getBlockState(commandContext_1, "block"), Mode.REPLACE, null)))
                    .then(literal("destroy")
                        .executes(c -> execute(c.getSource(), getLoadedBlockPos(c, "pos"), getBlockState(c, "block"), Mode.DESTROY, null)))
                    .then(literal("keep")
                        .executes(c -> execute(c.getSource(), getLoadedBlockPos(c, "pos"), getBlockState(c, "block"), Mode.REPLACE, (cachedBlockPosition) -> cachedBlockPosition.getWorld().isAir(cachedBlockPosition.getBlockPos()))))
                    .then(literal("replace")
                        .executes(c -> execute(c.getSource(), getLoadedBlockPos(c, "pos"), getBlockState(c, "block"), Mode.REPLACE, null)))
        ));
        commandDispatcher_1.register(carpetsetblock);
    }

    private static int execute(ServerCommandSource source, BlockPos pos, BlockStateArgument state, Mode mode, Predicate<CachedBlockPosition> filter) throws CommandSyntaxException {
        ServerWorld world = source.getWorld();
        if (filter != null && !filter.test(new CachedBlockPosition(world, pos, true))) throw FAILED_EXCEPTION.create();
        boolean shouldSetBlock;
        if (mode == Mode.DESTROY) {
            world.breakBlock(pos, true);
            shouldSetBlock = !state.getBlockState().isAir();
        } else {
            BlockEntity blockEntity_1 = world.getBlockEntity(pos);
            Clearable.clear(blockEntity_1);
            shouldSetBlock = true;
        }

        if (shouldSetBlock && !state.setBlockState(world, pos, SEND_TO_CLIENT | (Settings.fillUpdates ? 0 : NO_FILL_UPDATE))) throw FAILED_EXCEPTION.create();
        if (Settings.fillUpdates) world.updateNeighbors(pos, state.getBlockState().getBlock());
        source.sendFeedback(new TranslatableText("commands.setblock.success", pos.getX(), pos.getY(), pos.getZ()), true);
        return 1;
    }

    enum Mode {
        REPLACE,
        DESTROY
    }
}
