package quickcarpet.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.arguments.BlockPosArgumentType;
import net.minecraft.command.arguments.BlockPredicateArgumentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableIntBoundingBox;
import quickcarpet.settings.Settings;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class CarpetCloneCommand
{
    
    public static final Predicate<CachedBlockPosition> IS_AIR_PREDICATE = (cachedBlockPosition_1) -> {
        return !cachedBlockPosition_1.getBlockState().isAir();
    };
    private static final SimpleCommandExceptionType OVERLAP_EXCEPTION = new SimpleCommandExceptionType(new TranslatableComponent("commands.clone.overlap", new Object[0]));
    private static final Dynamic2CommandExceptionType TOOBIG_EXCEPTION = new Dynamic2CommandExceptionType((object_1, object_2) -> {
        return new TranslatableComponent("commands.clone.toobig", new Object[]{object_1, object_2});
    });
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableComponent("commands.clone.failed", new Object[0]));
    
    public static void register(CommandDispatcher<ServerCommandSource> commandDispatcher_1)
    {
        commandDispatcher_1.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) CommandManager.literal("carpetclone").requires((player) -> Settings.commandCarpetClone)).then(CommandManager.argument("begin", BlockPosArgumentType.create()).then(CommandManager.argument("end", BlockPosArgumentType.create()).then(((RequiredArgumentBuilder) ((RequiredArgumentBuilder) ((RequiredArgumentBuilder) CommandManager.argument("destination", BlockPosArgumentType.create()).executes((commandContext_1) -> {
            return method_13090((ServerCommandSource) commandContext_1.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "begin"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "end"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "destination"), (cachedBlockPosition_1) -> {
                return true;
            }, CarpetCloneCommand.class_3025.NORMAL);
        })).then(((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) CommandManager.literal("replace").executes((commandContext_1) -> {
            return method_13090((ServerCommandSource) commandContext_1.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "begin"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "end"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "destination"), (cachedBlockPosition_1) -> {
                return true;
            }, CarpetCloneCommand.class_3025.NORMAL);
        })).then(CommandManager.literal("force").executes((commandContext_1) -> {
            return method_13090((ServerCommandSource) commandContext_1.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "begin"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "end"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "destination"), (cachedBlockPosition_1) -> {
                return true;
            }, CarpetCloneCommand.class_3025.FORCE);
        }))).then(CommandManager.literal("move").executes((commandContext_1) -> {
            return method_13090((ServerCommandSource) commandContext_1.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "begin"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "end"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "destination"), (cachedBlockPosition_1) -> {
                return true;
            }, CarpetCloneCommand.class_3025.MOVE);
        }))).then(CommandManager.literal("normal").executes((commandContext_1) -> {
            return method_13090((ServerCommandSource) commandContext_1.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "begin"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "end"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "destination"), (cachedBlockPosition_1) -> {
                return true;
            }, CarpetCloneCommand.class_3025.NORMAL);
        })))).then(((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) CommandManager.literal("masked").executes((commandContext_1) -> {
            return method_13090((ServerCommandSource) commandContext_1.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "begin"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "end"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "destination"), IS_AIR_PREDICATE, CarpetCloneCommand.class_3025.NORMAL);
        })).then(CommandManager.literal("force").executes((commandContext_1) -> {
            return method_13090((ServerCommandSource) commandContext_1.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "begin"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "end"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "destination"), IS_AIR_PREDICATE, CarpetCloneCommand.class_3025.FORCE);
        }))).then(CommandManager.literal("move").executes((commandContext_1) -> {
            return method_13090((ServerCommandSource) commandContext_1.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "begin"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "end"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "destination"), IS_AIR_PREDICATE, CarpetCloneCommand.class_3025.MOVE);
        }))).then(CommandManager.literal("normal").executes((commandContext_1) -> {
            return method_13090((ServerCommandSource) commandContext_1.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "begin"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "end"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "destination"), IS_AIR_PREDICATE, CarpetCloneCommand.class_3025.NORMAL);
        })))).then(CommandManager.literal("filtered").then(((RequiredArgumentBuilder) ((RequiredArgumentBuilder) ((RequiredArgumentBuilder) CommandManager.argument("filter", BlockPredicateArgumentType.create()).executes((commandContext_1) -> {
            return method_13090((ServerCommandSource) commandContext_1.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "begin"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "end"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "destination"), BlockPredicateArgumentType.getBlockPredicate(commandContext_1, "filter"), CarpetCloneCommand.class_3025.NORMAL);
        })).then(CommandManager.literal("force").executes((commandContext_1) -> {
            return method_13090((ServerCommandSource) commandContext_1.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "begin"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "end"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "destination"), BlockPredicateArgumentType.getBlockPredicate(commandContext_1, "filter"), CarpetCloneCommand.class_3025.FORCE);
        }))).then(CommandManager.literal("move").executes((commandContext_1) -> {
            return method_13090((ServerCommandSource) commandContext_1.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "begin"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "end"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "destination"), BlockPredicateArgumentType.getBlockPredicate(commandContext_1, "filter"), CarpetCloneCommand.class_3025.MOVE);
        }))).then(CommandManager.literal("normal").executes((commandContext_1) -> {
            return method_13090((ServerCommandSource) commandContext_1.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "begin"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "end"), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "destination"), BlockPredicateArgumentType.getBlockPredicate(commandContext_1, "filter"), CarpetCloneCommand.class_3025.NORMAL);
        }))))))));
    }
    
    private static int method_13090(ServerCommandSource serverCommandSource_1, BlockPos blockPos_1, BlockPos blockPos_2, BlockPos blockPos_3, Predicate<CachedBlockPosition> predicate_1, CarpetCloneCommand.class_3025 cloneCommand$class_3025_1) throws CommandSyntaxException
    {
        MutableIntBoundingBox mutableIntBoundingBox_1 = new MutableIntBoundingBox(blockPos_1, blockPos_2);
        BlockPos blockPos_4 = blockPos_3.add(mutableIntBoundingBox_1.getSize());
        MutableIntBoundingBox mutableIntBoundingBox_2 = new MutableIntBoundingBox(blockPos_3, blockPos_4);
        if (!cloneCommand$class_3025_1.method_13109() && mutableIntBoundingBox_2.intersects(mutableIntBoundingBox_1))
        {
            throw OVERLAP_EXCEPTION.create();
        }
        else
        {
            int int_1 = mutableIntBoundingBox_1.getBlockCountX() * mutableIntBoundingBox_1.getBlockCountY() * mutableIntBoundingBox_1.getBlockCountZ();
            if (int_1 > Settings.fillLimit) // [CM] replaces 32768
            {
                throw TOOBIG_EXCEPTION.create(Settings.fillLimit, int_1);
            }
            else
            {
                ServerWorld serverWorld_1 = serverCommandSource_1.getWorld();
                if (serverWorld_1.isAreaLoaded(blockPos_1, blockPos_2) && serverWorld_1.isAreaLoaded(blockPos_3, blockPos_4))
                {
                    List<CarpetCloneCommand.class_3024> list_1 = Lists.newArrayList();
                    List<CarpetCloneCommand.class_3024> list_2 = Lists.newArrayList();
                    List<CarpetCloneCommand.class_3024> list_3 = Lists.newArrayList();
                    Deque<BlockPos> deque_1 = Lists.newLinkedList();
                    BlockPos blockPos_5 = new BlockPos(mutableIntBoundingBox_2.minX - mutableIntBoundingBox_1.minX, mutableIntBoundingBox_2.minY - mutableIntBoundingBox_1.minY, mutableIntBoundingBox_2.minZ - mutableIntBoundingBox_1.minZ);
                    
                    int int_5;
                    for (int int_2 = mutableIntBoundingBox_1.minZ; int_2 <= mutableIntBoundingBox_1.maxZ; ++int_2)
                    {
                        for (int int_3 = mutableIntBoundingBox_1.minY; int_3 <= mutableIntBoundingBox_1.maxY; ++int_3)
                        {
                            for (int_5 = mutableIntBoundingBox_1.minX; int_5 <= mutableIntBoundingBox_1.maxX; ++int_5)
                            {
                                BlockPos blockPos_6 = new BlockPos(int_5, int_3, int_2);
                                BlockPos blockPos_7 = blockPos_6.add(blockPos_5);
                                CachedBlockPosition cachedBlockPosition_1 = new CachedBlockPosition(serverWorld_1, blockPos_6, false);
                                BlockState blockState_1 = cachedBlockPosition_1.getBlockState();
                                if (predicate_1.test(cachedBlockPosition_1))
                                {
                                    BlockEntity blockEntity_1 = serverWorld_1.getBlockEntity(blockPos_6);
                                    if (blockEntity_1 != null)
                                    {
                                        CompoundTag compoundTag_1 = blockEntity_1.toTag(new CompoundTag());
                                        list_2.add(new CarpetCloneCommand.class_3024(blockPos_7, blockState_1, compoundTag_1));
                                        deque_1.addLast(blockPos_6);
                                    }
                                    else if (!blockState_1.isFullOpaque(serverWorld_1, blockPos_6) && !Block.isShapeFullCube(blockState_1.getCollisionShape(serverWorld_1, blockPos_6)))
                                    {
                                        list_3.add(new CarpetCloneCommand.class_3024(blockPos_7, blockState_1, (CompoundTag) null));
                                        deque_1.addFirst(blockPos_6);
                                    }
                                    else
                                    {
                                        list_1.add(new CarpetCloneCommand.class_3024(blockPos_7, blockState_1, (CompoundTag) null));
                                        deque_1.addLast(blockPos_6);
                                    }
                                }
                            }
                        }
                    }
                    
                    if (cloneCommand$class_3025_1 == CarpetCloneCommand.class_3025.MOVE)
                    {
                        Iterator var25 = deque_1.iterator();
                        
                        BlockPos blockPos_9;
                        while (var25.hasNext())
                        {
                            blockPos_9 = (BlockPos) var25.next();
                            BlockEntity blockEntity_2 = serverWorld_1.getBlockEntity(blockPos_9);
                            Clearable.clear(blockEntity_2);
                            serverWorld_1.setBlockState(blockPos_9, Blocks.BARRIER.getDefaultState(), 2 | (Settings.fillUpdates ? 0 : 1024));
                        }
                        
                        var25 = deque_1.iterator();
                        
                        while (var25.hasNext())
                        {
                            blockPos_9 = (BlockPos) var25.next();
                            serverWorld_1.setBlockState(blockPos_9, Blocks.AIR.getDefaultState(), 3 | (Settings.fillUpdates ? 0 : 1024));
                        }
                    }
                    
                    List<CarpetCloneCommand.class_3024> list_4 = Lists.newArrayList();
                    list_4.addAll(list_1);
                    list_4.addAll(list_2);
                    list_4.addAll(list_3);
                    List<CarpetCloneCommand.class_3024> list_5 = Lists.reverse(list_4);
                    Iterator var30 = list_5.iterator();
                    
                    while (var30.hasNext())
                    {
                        CarpetCloneCommand.class_3024 cloneCommand$class_3024_1 = (CarpetCloneCommand.class_3024) var30.next();
                        BlockEntity blockEntity_3 = serverWorld_1.getBlockEntity(cloneCommand$class_3024_1.field_13496);
                        Clearable.clear(blockEntity_3);
                        serverWorld_1.setBlockState(cloneCommand$class_3024_1.field_13496, Blocks.BARRIER.getDefaultState(), 2 | (Settings.fillUpdates ? 0 : 1024));
                    }
                    
                    int_5 = 0;
                    Iterator var32 = list_4.iterator();
                    
                    CarpetCloneCommand.class_3024 cloneCommand$class_3024_4;
                    while (var32.hasNext())
                    {
                        cloneCommand$class_3024_4 = (CarpetCloneCommand.class_3024) var32.next();
                        if (serverWorld_1.setBlockState(cloneCommand$class_3024_4.field_13496, cloneCommand$class_3024_4.field_13495, 2 | (Settings.fillUpdates ? 0 : 1024)))
                        {
                            ++int_5;
                        }
                    }
                    
                    for (var32 = list_2.iterator(); var32.hasNext(); serverWorld_1.setBlockState(cloneCommand$class_3024_4.field_13496, cloneCommand$class_3024_4.field_13495, 2 | (Settings.fillUpdates ? 0 : 1024)))
                    {
                        cloneCommand$class_3024_4 = (CarpetCloneCommand.class_3024) var32.next();
                        BlockEntity blockEntity_4 = serverWorld_1.getBlockEntity(cloneCommand$class_3024_4.field_13496);
                        if (cloneCommand$class_3024_4.field_13494 != null && blockEntity_4 != null)
                        {
                            cloneCommand$class_3024_4.field_13494.putInt("x", cloneCommand$class_3024_4.field_13496.getX());
                            cloneCommand$class_3024_4.field_13494.putInt("y", cloneCommand$class_3024_4.field_13496.getY());
                            cloneCommand$class_3024_4.field_13494.putInt("z", cloneCommand$class_3024_4.field_13496.getZ());
                            blockEntity_4.fromTag(cloneCommand$class_3024_4.field_13494);
                            blockEntity_4.markDirty();
                        }
                    }
                    
                    var32 = list_5.iterator();
                    
                    if (Settings.fillUpdates)
                    {
                        while (var32.hasNext())
                        {
                            cloneCommand$class_3024_4 = (CarpetCloneCommand.class_3024) var32.next();
                            serverWorld_1.updateNeighbors(cloneCommand$class_3024_4.field_13496, cloneCommand$class_3024_4.field_13495.getBlock());
                        }
                        
                        serverWorld_1.method_14196().copyScheduledTicks(mutableIntBoundingBox_1, blockPos_5);
                    }
                    
                    if (int_5 == 0)
                    {
                        throw FAILED_EXCEPTION.create();
                    }
                    else
                    {
                        serverCommandSource_1.sendFeedback(new TranslatableComponent("commands.clone.success", new Object[]{int_5}), true);
                        return int_5;
                    }
                }
                else
                {
                    throw BlockPosArgumentType.UNLOADED_EXCEPTION.create();
                }
            }
        }
    }
    
    static enum class_3025
    {
        FORCE(true), MOVE(true), NORMAL(false);
        
        private final boolean field_13498;
        
        private class_3025(boolean boolean_1)
        {
            this.field_13498 = boolean_1;
        }
        
        public boolean method_13109()
        {
            return this.field_13498;
        }
    }
    
    static class class_3024
    {
        public final BlockPos field_13496;
        public final BlockState field_13495;
        public final CompoundTag field_13494;
        
        public class_3024(BlockPos blockPos_1, BlockState blockState_1, CompoundTag compoundTag_1)
        {
            this.field_13496 = blockPos_1;
            this.field_13495 = blockState_1;
            this.field_13494 = compoundTag_1;
        }
    }
}
