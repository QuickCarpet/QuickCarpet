package quickcarpet.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.SetBlockCommand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import quickcarpet.settings.Settings;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static net.minecraft.command.arguments.BlockPosArgumentType.blockPos;
import static net.minecraft.command.arguments.BlockPosArgumentType.getLoadedBlockPos;
import static net.minecraft.command.arguments.BlockPredicateArgumentType.blockPredicate;
import static net.minecraft.command.arguments.BlockPredicateArgumentType.getBlockPredicate;
import static net.minecraft.command.arguments.BlockStateArgumentType.blockState;
import static net.minecraft.command.arguments.BlockStateArgumentType.getBlockState;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static quickcarpet.utils.Constants.SetBlockState.NO_FILL_UPDATE;
import static quickcarpet.utils.Constants.SetBlockState.SEND_TO_CLIENT;

public class CarpetFillCommand {

    private static final Dynamic2CommandExceptionType TOOBIG_EXCEPTION = new Dynamic2CommandExceptionType((a, b) -> new TranslatableText("commands.fill.toobig", a, b));
    private static final BlockStateArgument AIR_BLOCK_ARGUMENT;
    private static final SimpleCommandExceptionType FAILED_EXCEPTION;

    static {
        AIR_BLOCK_ARGUMENT = new BlockStateArgument(Blocks.AIR.getDefaultState(), Collections.emptySet(), null);
        FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.fill.failed"));
    }

    public static void register(CommandDispatcher<ServerCommandSource> commandDispatcher_1) {
        LiteralArgumentBuilder<ServerCommandSource> carpetfill = literal("carpetfill")
            .requires(s -> s.hasPermissionLevel(Settings.commandCarpetFill))
            .then(argument("from", blockPos())
            .then(argument("to", blockPos())
            .then(argument("block", blockState())
                .executes(c -> execute(c.getSource(), new BlockBox(getLoadedBlockPos(c, "from"), getLoadedBlockPos(c, "to")), getBlockState(c, "block"), Mode.REPLACE, null))
                    .then(literal("replace")
                        .executes(c -> execute(c.getSource(), new BlockBox(getLoadedBlockPos(c, "from"), getLoadedBlockPos(c, "to")), getBlockState(c, "block"), Mode.REPLACE, null))
                        .then(argument("filter", blockPredicate())
                            .executes(c -> execute(c.getSource(), new BlockBox(getLoadedBlockPos(c, "from"), getLoadedBlockPos(c, "to")), getBlockState(c, "block"), Mode.REPLACE, getBlockPredicate(c, "filter"))))
                    ).then(literal("keep")
                        .executes(c -> execute(c.getSource(), new BlockBox(getLoadedBlockPos(c, "from"), getLoadedBlockPos(c, "to")), getBlockState(c, "block"), Mode.REPLACE, cachedBlockPosition -> cachedBlockPosition.getWorld().isAir(cachedBlockPosition.getBlockPos()))))
                    .then(literal("outline")
                        .executes(c -> execute(c.getSource(), new BlockBox(getLoadedBlockPos(c, "from"), getLoadedBlockPos(c, "to")), getBlockState(c, "block"), Mode.OUTLINE, null)))
                    .then(literal("hollow")
                        .executes(c -> execute(c.getSource(), new BlockBox(getLoadedBlockPos(c, "from"), getLoadedBlockPos(c, "to")), getBlockState(c, "block"), Mode.HOLLOW, null)))
                    .then(literal("destroy")
                        .executes(c -> execute(c.getSource(), new BlockBox(getLoadedBlockPos(c, "from"), getLoadedBlockPos(c, "to")), getBlockState(c, "block"), Mode.DESTROY, null)))
        )));
        commandDispatcher_1.register(carpetfill);
    }

    private static int execute(ServerCommandSource source, BlockBox box, BlockStateArgument state, Mode mode, Predicate<CachedBlockPosition> filter) throws CommandSyntaxException {
        int volume = box.getBlockCountX() * box.getBlockCountY() * box.getBlockCountZ();
        if (volume > Settings.fillLimit) { // [CM] replaces 32768
            throw TOOBIG_EXCEPTION.create(Settings.fillLimit, volume);
        } else {
            List<BlockPos> list_1 = Lists.newArrayList();
            ServerWorld world = source.getWorld();
            int filled = 0;

            for (BlockPos pos : BlockPos.iterate(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)) {
                if (filter != null && !filter.test(new CachedBlockPosition(world, pos, true))) continue;

                BlockStateArgument blockArgument_2 = mode.filter.filter(box, pos, state, world);
                if (blockArgument_2 != null) {
                    BlockEntity blockEntity_1 = world.getBlockEntity(pos);
                    Clearable.clear(blockEntity_1);
                    if (blockArgument_2.setBlockState(world, pos, SEND_TO_CLIENT | (Settings.fillUpdates ? 0 : NO_FILL_UPDATE))) {
                        list_1.add(pos.toImmutable());
                        ++filled;
                    }
                }
            }

            if (Settings.fillUpdates) {
                for (BlockPos pos : list_1) {
                    world.updateNeighbors(pos, world.getBlockState(pos).getBlock());
                }
            }

            if (filled == 0) {
                throw FAILED_EXCEPTION.create();
            }

            source.sendFeedback(new TranslatableText("commands.fill.success", filled), true);
            return filled;
        }
    }
    
    enum Mode {
        REPLACE((box, pos, state, world) -> state),
        OUTLINE((box, pos, state, world) -> pos.getX() != box.minX && pos.getX() != box.maxX && pos.getY() != box.minY && pos.getY() != box.maxY && pos.getZ() != box.minZ && pos.getZ() != box.maxZ ? null : state),
        HOLLOW((box, pos, state, world) -> pos.getX() != box.minX && pos.getX() != box.maxX && pos.getY() != box.minY && pos.getY() != box.maxY && pos.getZ() != box.minZ && pos.getZ() != box.maxZ ? CarpetFillCommand.AIR_BLOCK_ARGUMENT : state),
        DESTROY((box, pos, state, world) -> {
            world.breakBlock(pos, true);
            return state;
        });
        
        public final SetBlockCommand.Filter filter;
        
        Mode(SetBlockCommand.Filter filter) {
            this.filter = filter;
        }
    }
}
