package quickcarpet.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
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
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import quickcarpet.settings.Settings;

import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;

import static net.minecraft.command.arguments.BlockPosArgumentType.blockPos;
import static net.minecraft.command.arguments.BlockPosArgumentType.getLoadedBlockPos;
import static net.minecraft.command.arguments.BlockPredicateArgumentType.blockPredicate;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static quickcarpet.utils.Constants.SetBlockState.*;

public class CarpetCloneCommand {
    
    public static final Predicate<CachedBlockPosition> NOT_AIR_PREDICATE = cachedBlockPosition -> !cachedBlockPosition.getBlockState().isAir();
    private static final SimpleCommandExceptionType OVERLAP_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.clone.overlap"));
    private static final Dynamic2CommandExceptionType TOOBIG_EXCEPTION = new Dynamic2CommandExceptionType((a, b) -> new TranslatableText("commands.clone.toobig", a, b));
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.clone.failed"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> carpetclone = literal("carpetclone")
            .requires(s -> s.hasPermissionLevel(Settings.commandCarpetClone))
            .then(argument("begin", blockPos())
            .then(argument("end", blockPos())
            .then((argument("destination", blockPos())
                .executes(ctx -> execute(ctx.getSource(), getLoadedBlockPos(ctx, "begin"), getLoadedBlockPos(ctx, "end"), getLoadedBlockPos(ctx, "destination"), cachedBlockPosition -> true, Mode.NORMAL)))
                .then((((literal("replace")
                    .executes(ctx -> execute(ctx.getSource(), getLoadedBlockPos(ctx, "begin"), getLoadedBlockPos(ctx, "end"), getLoadedBlockPos(ctx, "destination"), cachedBlockPosition -> true, Mode.NORMAL)))
                    .then(literal("force")
                        .executes(ctx -> execute(ctx.getSource(), getLoadedBlockPos(ctx, "begin"), getLoadedBlockPos(ctx, "end"), getLoadedBlockPos(ctx, "destination"), cachedBlockPosition -> true, Mode.FORCE))))
                    .then(literal("move")
                        .executes(ctx -> execute(ctx.getSource(), getLoadedBlockPos(ctx, "begin"), getLoadedBlockPos(ctx, "end"), getLoadedBlockPos(ctx, "destination"), cachedBlockPosition -> true, Mode.MOVE))))
                    .then(literal("normal")
                        .executes(ctx -> execute(ctx.getSource(), getLoadedBlockPos(ctx, "begin"), getLoadedBlockPos(ctx, "end"), getLoadedBlockPos(ctx, "destination"), cachedBlockPosition -> true, Mode.NORMAL))))
                .then((((literal("masked")
                    .executes(ctx -> execute(ctx.getSource(), getLoadedBlockPos(ctx, "begin"), getLoadedBlockPos(ctx, "end"), getLoadedBlockPos(ctx, "destination"), NOT_AIR_PREDICATE, Mode.NORMAL)))
                    .then(literal("force")
                        .executes(ctx -> execute(ctx.getSource(), getLoadedBlockPos(ctx, "begin"), getLoadedBlockPos(ctx, "end"), getLoadedBlockPos(ctx, "destination"), NOT_AIR_PREDICATE, Mode.FORCE))))
                    .then(literal("move")
                        .executes(ctx -> execute(ctx.getSource(), getLoadedBlockPos(ctx, "begin"), getLoadedBlockPos(ctx, "end"), getLoadedBlockPos(ctx, "destination"), NOT_AIR_PREDICATE, Mode.MOVE))))
                    .then(literal("normal")
                        .executes(ctx -> execute(ctx.getSource(), getLoadedBlockPos(ctx, "begin"), getLoadedBlockPos(ctx, "end"), getLoadedBlockPos(ctx, "destination"), NOT_AIR_PREDICATE, Mode.NORMAL))))
                    .then(literal("filtered")
                        .then((((argument("filter", blockPredicate())
                            .executes((ctx) -> execute(ctx.getSource(), getLoadedBlockPos(ctx, "begin"), getLoadedBlockPos(ctx, "end"), getLoadedBlockPos(ctx, "destination"), BlockPredicateArgumentType.getBlockPredicate(ctx, "filter"), Mode.NORMAL)))
                            .then(literal("force")
                                    .executes(ctx -> execute(ctx.getSource(), getLoadedBlockPos(ctx, "begin"), getLoadedBlockPos(ctx, "end"), getLoadedBlockPos(ctx, "destination"), BlockPredicateArgumentType.getBlockPredicate(ctx, "filter"), Mode.FORCE))))
                            .then(literal("move")
                                    .executes(ctx -> execute(ctx.getSource(), getLoadedBlockPos(ctx, "begin"), getLoadedBlockPos(ctx, "end"), getLoadedBlockPos(ctx, "destination"), BlockPredicateArgumentType.getBlockPredicate(ctx, "filter"), Mode.MOVE))))
                            .then(literal("normal")
                                    .executes(ctx -> execute(ctx.getSource(), getLoadedBlockPos(ctx, "begin"), getLoadedBlockPos(ctx, "end"), getLoadedBlockPos(ctx, "destination"), BlockPredicateArgumentType.getBlockPredicate(ctx, "filter"), Mode.NORMAL))))))));
        dispatcher.register(carpetclone);
    }

    private static int execute(ServerCommandSource source, BlockPos begin, BlockPos end, BlockPos destination, Predicate<CachedBlockPosition> filter, Mode mode) throws CommandSyntaxException {
        BlockBox sourceBox = new BlockBox(begin, end);
        BlockPos destinationEnd = destination.add(sourceBox.getDimensions());
        BlockBox destinationBox = new BlockBox(destination, destinationEnd);
        if (!mode.allowsOverlap() && destinationBox.intersects(sourceBox)) {
            throw OVERLAP_EXCEPTION.create();
        } else {
            int volume = sourceBox.getBlockCountX() * sourceBox.getBlockCountY() * sourceBox.getBlockCountZ();
            if (volume > Settings.fillLimit) // [CM] replaces 32768
            {
                throw TOOBIG_EXCEPTION.create(Settings.fillLimit, volume);
            } else {
                ServerWorld world = source.getWorld();
                if (world.isRegionLoaded(begin, end) && world.isRegionLoaded(destination, destinationEnd)) {
                    List<BlockInfo> otherBlocks = Lists.newArrayList();
                    List<BlockInfo> blocksWithEntity = Lists.newArrayList();
                    List<BlockInfo> fullBlocks = Lists.newArrayList();
                    Deque<BlockPos> updateOrder = Lists.newLinkedList();
                    BlockPos relative = new BlockPos(destinationBox.minX - sourceBox.minX, destinationBox.minY - sourceBox.minY, destinationBox.minZ - sourceBox.minZ);

                    for (int z = sourceBox.minZ; z <= sourceBox.maxZ; ++z) {
                        for (int y = sourceBox.minY; y <= sourceBox.maxY; ++y) {
                            for (int x = sourceBox.minX; x <= sourceBox.maxX; ++x) {
                                BlockPos srcPos = new BlockPos(x, y, z);
                                BlockPos destPos = srcPos.add(relative);
                                CachedBlockPosition cachedBlockPosition = new CachedBlockPosition(world, srcPos, false);
                                BlockState state = cachedBlockPosition.getBlockState();
                                if (filter.test(cachedBlockPosition)) {
                                    BlockEntity blockEntity = world.getBlockEntity(srcPos);
                                    if (blockEntity != null) {
                                        CompoundTag blockEntityTag = blockEntity.toTag(new CompoundTag());
                                        blocksWithEntity.add(new BlockInfo(destPos, state, blockEntityTag));
                                        updateOrder.addLast(srcPos);
                                    } else if (!state.isFullOpaque(world, srcPos) && !Block.isShapeFullCube(state.getCollisionShape(world, srcPos))) {
                                        fullBlocks.add(new BlockInfo(destPos, state, null));
                                        updateOrder.addFirst(srcPos);
                                    } else {
                                        otherBlocks.add(new BlockInfo(destPos, state, null));
                                        updateOrder.addLast(srcPos);
                                    }
                                }
                            }
                        }
                    }

                    if (mode == Mode.MOVE) {
                        for (BlockPos blockPos : updateOrder) {
                            Clearable.clear(world.getBlockEntity(blockPos));
                            world.setBlockState(blockPos, Blocks.BARRIER.getDefaultState(), SEND_TO_CLIENT | (Settings.fillUpdates ? 0 : NO_FILL_UPDATE));
                        }

                        for (BlockPos blockPos : updateOrder) {
                            world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), SEND_TO_CLIENT | (Settings.fillUpdates ? UPDATE_NEIGHBORS : NO_FILL_UPDATE));
                        }
                    }

                    List<BlockInfo> changes = Lists.newArrayList();
                    changes.addAll(otherBlocks);
                    changes.addAll(blocksWithEntity);
                    changes.addAll(fullBlocks);
                    List<BlockInfo> reversedChanges = Lists.reverse(changes);

                    for (BlockInfo blockInfo_1 : reversedChanges) {
                        Clearable.clear(world.getBlockEntity(blockInfo_1.pos));
                        world.setBlockState(blockInfo_1.pos, Blocks.BARRIER.getDefaultState(), SEND_TO_CLIENT | (Settings.fillUpdates ? 0 : NO_FILL_UPDATE));
                    }

                    int numBlocks = 0;

                    for (BlockInfo blockInfo : changes) {
                        if (world.setBlockState(blockInfo.pos, blockInfo.state, SEND_TO_CLIENT | (Settings.fillUpdates ? 0 : NO_FILL_UPDATE))) {
                            ++numBlocks;
                        }
                    }

                    for (BlockInfo info : blocksWithEntity) {
                        BlockEntity blockEntity = world.getBlockEntity(info.pos);
                        if (info.blockEntityTag != null && blockEntity != null) {
                            info.blockEntityTag.putInt("x", info.pos.getX());
                            info.blockEntityTag.putInt("y", info.pos.getY());
                            info.blockEntityTag.putInt("z", info.pos.getZ());
                            blockEntity.fromTag(info.blockEntityTag);
                            blockEntity.markDirty();
                        }
                        world.setBlockState(info.pos, info.state, SEND_TO_CLIENT | (Settings.fillUpdates ? 0 : NO_FILL_UPDATE));
                    }

                    if (Settings.fillUpdates) {
                        for (BlockInfo blockInfo : reversedChanges) {
                            world.updateNeighbors(blockInfo.pos, blockInfo.state.getBlock());
                        }

                        world.getBlockTickScheduler().copyScheduledTicks(sourceBox, relative);
                    }

                    if (numBlocks == 0) {
                        throw FAILED_EXCEPTION.create();
                    } else {
                        source.sendFeedback(new TranslatableText("commands.clone.success", numBlocks), true);
                        return numBlocks;
                    }
                } else {
                    throw BlockPosArgumentType.UNLOADED_EXCEPTION.create();
                }
            }
        }
    }
    
    enum Mode {
        FORCE(true), MOVE(true), NORMAL(false);
        
        private final boolean allowsOverlap;
        
        Mode(boolean boolean_1) {
            this.allowsOverlap = boolean_1;
        }
        
        public boolean allowsOverlap() {
            return this.allowsOverlap;
        }
    }
    
    static class BlockInfo {
        public final BlockPos pos;
        public final BlockState state;
        public final CompoundTag blockEntityTag;
        
        public BlockInfo(BlockPos pos, BlockState state, CompoundTag blockEntityTag) {
            this.pos = pos;
            this.state = state;
            this.blockEntityTag = blockEntityTag;
        }
    }
}
