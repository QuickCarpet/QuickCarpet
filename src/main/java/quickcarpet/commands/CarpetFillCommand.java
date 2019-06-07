package quickcarpet.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.arguments.BlockPosArgumentType;
import net.minecraft.command.arguments.BlockPredicateArgumentType;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.command.arguments.BlockStateArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.SetBlockCommand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableIntBoundingBox;
import quickcarpet.settings.Settings;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class CarpetFillCommand {

    private static final Dynamic2CommandExceptionType TOOBIG_EXCEPTION = new Dynamic2CommandExceptionType((object_1, object_2) -> new TranslatableText("commands.fill.toobig", object_1, object_2));
    private static final BlockStateArgument field_13648;
    private static final SimpleCommandExceptionType FAILED_EXCEPTION;

    static {
        field_13648 = new BlockStateArgument(Blocks.AIR.getDefaultState(), Collections.emptySet(), null);
        FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.fill.failed"));
    }

    public static void register(CommandDispatcher<ServerCommandSource> commandDispatcher_1) {
        commandDispatcher_1.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) CommandManager.literal("carpetfill").requires((player) ->
            Settings.commandCarpetFill
        )).then(CommandManager.argument("from", BlockPosArgumentType.create()).then(CommandManager.argument("to", BlockPosArgumentType.create()).then(((RequiredArgumentBuilder) ((RequiredArgumentBuilder) ((RequiredArgumentBuilder) ((RequiredArgumentBuilder) ((RequiredArgumentBuilder) CommandManager.argument("block", BlockStateArgumentType.create()).executes((commandContext_1) -> {
            return method_13354((ServerCommandSource) commandContext_1.getSource(), new MutableIntBoundingBox(BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "from"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "to")), BlockStateArgumentType.getBlockState(commandContext_1, "block"), CarpetFillCommand.class_3058.REPLACE, (Predicate) null);
        })).then(((LiteralArgumentBuilder) CommandManager.literal("replace").executes((commandContext_1) -> {
            return method_13354((ServerCommandSource) commandContext_1.getSource(), new MutableIntBoundingBox(BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "from"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "to")), BlockStateArgumentType.getBlockState(commandContext_1, "block"), CarpetFillCommand.class_3058.REPLACE, (Predicate) null);
        })).then(CommandManager.argument("filter", BlockPredicateArgumentType.create()).executes((commandContext_1) -> {
            return method_13354((ServerCommandSource) commandContext_1.getSource(), new MutableIntBoundingBox(BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "from"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "to")), BlockStateArgumentType.getBlockState(commandContext_1, "block"), CarpetFillCommand.class_3058.REPLACE, BlockPredicateArgumentType.getBlockPredicate(commandContext_1, "filter"));
        })))).then(CommandManager.literal("keep").executes((commandContext_1) -> {
            return method_13354((ServerCommandSource) commandContext_1.getSource(), new MutableIntBoundingBox(BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "from"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "to")), BlockStateArgumentType.getBlockState(commandContext_1, "block"), CarpetFillCommand.class_3058.REPLACE, (cachedBlockPosition_1) -> {
                return cachedBlockPosition_1.getWorld().isAir(cachedBlockPosition_1.getBlockPos());
            });
        }))).then(CommandManager.literal("outline").executes((commandContext_1) -> {
            return method_13354((ServerCommandSource) commandContext_1.getSource(), new MutableIntBoundingBox(BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "from"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "to")), BlockStateArgumentType.getBlockState(commandContext_1, "block"), CarpetFillCommand.class_3058.OUTLINE, (Predicate) null);
        }))).then(CommandManager.literal("hollow").executes((commandContext_1) -> {
            return method_13354((ServerCommandSource) commandContext_1.getSource(), new MutableIntBoundingBox(BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "from"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "to")), BlockStateArgumentType.getBlockState(commandContext_1, "block"), CarpetFillCommand.class_3058.HOLLOW, (Predicate) null);
        }))).then(CommandManager.literal("destroy").executes((commandContext_1) -> {
            return method_13354((ServerCommandSource) commandContext_1.getSource(), new MutableIntBoundingBox(BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "from"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "to")), BlockStateArgumentType.getBlockState(commandContext_1, "block"), CarpetFillCommand.class_3058.DESTROY, (Predicate) null);
        }))))));
    }

    private static int method_13354(ServerCommandSource serverCommandSource_1, MutableIntBoundingBox mutableIntBoundingBox_1, BlockStateArgument blockArgument_1, CarpetFillCommand.class_3058 fillCommand$class_3058_1, Predicate<CachedBlockPosition> predicate_1) throws CommandSyntaxException {
        int int_1 = mutableIntBoundingBox_1.getBlockCountX() * mutableIntBoundingBox_1.getBlockCountY() * mutableIntBoundingBox_1.getBlockCountZ();
        if (int_1 > Settings.fillLimit) // [CM] replaces 32768
        {
            throw TOOBIG_EXCEPTION.create(Settings.fillLimit, int_1);
        } else {
            List<BlockPos> list_1 = Lists.newArrayList();
            ServerWorld serverWorld_1 = serverCommandSource_1.getWorld();
            int int_2 = 0;
            Iterator var9 = BlockPos.iterate(mutableIntBoundingBox_1.minX, mutableIntBoundingBox_1.minY, mutableIntBoundingBox_1.minZ, mutableIntBoundingBox_1.maxX, mutableIntBoundingBox_1.maxY, mutableIntBoundingBox_1.maxZ).iterator();

            while (true) {
                BlockPos blockPos_1;
                do {
                    if (!var9.hasNext()) {
                        var9 = list_1.iterator();

                        if (Settings.fillUpdates)
                        {
                            while (var9.hasNext()) {
                                blockPos_1 = (BlockPos) var9.next();
                                Block block_1 = serverWorld_1.getBlockState(blockPos_1).getBlock();
                                serverWorld_1.updateNeighbors(blockPos_1, block_1);
                            }
                        }

                        if (int_2 == 0) {
                            throw FAILED_EXCEPTION.create();
                        }

                        serverCommandSource_1.sendFeedback(new TranslatableText("commands.fill.success", int_2), true);
                        return int_2;
                    }

                    blockPos_1 = (BlockPos) var9.next();
                } while (predicate_1 != null && !predicate_1.test(new CachedBlockPosition(serverWorld_1, blockPos_1, true)));

                BlockStateArgument blockArgument_2 = fillCommand$class_3058_1.filter.filter(mutableIntBoundingBox_1, blockPos_1, blockArgument_1, serverWorld_1);
                if (blockArgument_2 != null) {
                    BlockEntity blockEntity_1 = serverWorld_1.getBlockEntity(blockPos_1);
                    Clearable.clear(blockEntity_1);
                    if (blockArgument_2.setBlockState(serverWorld_1, blockPos_1, 2 | (Settings.fillUpdates?0:1024))) {
                        list_1.add(blockPos_1.toImmutable());
                        ++int_2;
                    }
                }
            }
        }
    }
    
    static enum class_3058 {
        REPLACE((mutableIntBoundingBox_1, blockPos_1, blockStateArgument_1, serverWorld_1) -> {
            return blockStateArgument_1;
        }),
        OUTLINE((mutableIntBoundingBox_1, blockPos_1, blockStateArgument_1, serverWorld_1) -> {
            return blockPos_1.getX() != mutableIntBoundingBox_1.minX && blockPos_1.getX() != mutableIntBoundingBox_1.maxX && blockPos_1.getY() != mutableIntBoundingBox_1.minY && blockPos_1.getY() != mutableIntBoundingBox_1.maxY && blockPos_1.getZ() != mutableIntBoundingBox_1.minZ && blockPos_1.getZ() != mutableIntBoundingBox_1.maxZ ? null : blockStateArgument_1;
        }),
        HOLLOW((mutableIntBoundingBox_1, blockPos_1, blockStateArgument_1, serverWorld_1) -> {
            return blockPos_1.getX() != mutableIntBoundingBox_1.minX && blockPos_1.getX() != mutableIntBoundingBox_1.maxX && blockPos_1.getY() != mutableIntBoundingBox_1.minY && blockPos_1.getY() != mutableIntBoundingBox_1.maxY && blockPos_1.getZ() != mutableIntBoundingBox_1.minZ && blockPos_1.getZ() != mutableIntBoundingBox_1.maxZ ? CarpetFillCommand.field_13648 : blockStateArgument_1;
        }),
        DESTROY((mutableIntBoundingBox_1, blockPos_1, blockStateArgument_1, serverWorld_1) -> {
            serverWorld_1.breakBlock(blockPos_1, true);
            return blockStateArgument_1;
        });
        
        public final SetBlockCommand.Filter filter;
        
        private class_3058(SetBlockCommand.Filter setBlockCommand$Filter_1) {
            this.filter = setBlockCommand$Filter_1;
        }
    }
}
