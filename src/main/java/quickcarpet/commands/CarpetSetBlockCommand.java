package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.command.arguments.BlockStateArgumentType;
import net.minecraft.command.arguments.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableIntBoundingBox;
import quickcarpet.QuickCarpetSettings;

import java.util.function.Predicate;

public class CarpetSetBlockCommand {
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableTextComponent("commands.setblock.failed", new Object[0]));

    public static void register(CommandDispatcher<ServerCommandSource> commandDispatcher_1) {
        commandDispatcher_1.register((LiteralArgumentBuilder)((LiteralArgumentBuilder) CommandManager.literal("carpetsetblock").requires((player) ->
                QuickCarpetSettings.getBool("")
        )).then(CommandManager.argument("pos", BlockPosArgumentType.create()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("block", BlockStateArgumentType.create()).executes((commandContext_1) -> {
            return method_13620((ServerCommandSource)commandContext_1.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "pos"), BlockStateArgumentType.getBlockState(commandContext_1, "block"), CarpetSetBlockCommand.class_3121.REPLACE, (Predicate)null);
        })).then(CommandManager.literal("destroy").executes((commandContext_1) -> {
            return method_13620((ServerCommandSource)commandContext_1.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "pos"), BlockStateArgumentType.getBlockState(commandContext_1, "block"), CarpetSetBlockCommand.class_3121.DESTROY, (Predicate)null);
        }))).then(CommandManager.literal("keep").executes((commandContext_1) -> {
            return method_13620((ServerCommandSource)commandContext_1.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "pos"), BlockStateArgumentType.getBlockState(commandContext_1, "block"), CarpetSetBlockCommand.class_3121.REPLACE, (cachedBlockPosition_1) -> {
                return cachedBlockPosition_1.getWorld().isAir(cachedBlockPosition_1.getBlockPos());
            });
        }))).then(CommandManager.literal("replace").executes((commandContext_1) -> {
            return method_13620((ServerCommandSource)commandContext_1.getSource(), BlockPosArgumentType.getLoadedBlockPos(commandContext_1, "pos"), BlockStateArgumentType.getBlockState(commandContext_1, "block"), CarpetSetBlockCommand.class_3121.REPLACE, (Predicate)null);
        })))));
    }

    private static int method_13620(ServerCommandSource serverCommandSource_1, BlockPos blockPos_1, BlockStateArgument blockArgument_1, CarpetSetBlockCommand.class_3121 setBlockCommand$class_3121_1, Predicate<CachedBlockPosition> predicate_1) throws CommandSyntaxException {
        ServerWorld serverWorld_1 = serverCommandSource_1.getWorld();
        if (predicate_1 != null && !predicate_1.test(new CachedBlockPosition(serverWorld_1, blockPos_1, true))) {
            throw FAILED_EXCEPTION.create();
        } else {
            boolean boolean_2;
            if (setBlockCommand$class_3121_1 == CarpetSetBlockCommand.class_3121.DESTROY) {
                serverWorld_1.breakBlock(blockPos_1, true);
                boolean_2 = !blockArgument_1.getBlockState().isAir();
            } else {
                BlockEntity blockEntity_1 = serverWorld_1.getBlockEntity(blockPos_1);
                Clearable.clear(blockEntity_1);
                boolean_2 = true;
            }

            if (boolean_2 && !blockArgument_1.setBlockState(serverWorld_1, blockPos_1, 2 | (QuickCarpetSettings.getBool("fillUpdates")?0:128))) {
                throw FAILED_EXCEPTION.create();
            } else {
                if (QuickCarpetSettings.getBool("fillUpdates"))
                {
                    serverWorld_1.updateNeighbors(blockPos_1, blockArgument_1.getBlockState().getBlock());
                }
                serverCommandSource_1.sendFeedback(new TranslatableTextComponent("commands.setblock.success", new Object[]{blockPos_1.getX(), blockPos_1.getY(), blockPos_1.getZ()}), true);
                return 1;
            }
        }
    }

    interface class_3120 {
        BlockStateArgument filter(MutableIntBoundingBox var1, BlockPos var2, BlockStateArgument var3, ServerWorld var4);
    }

    enum class_3121 {
        REPLACE,
        DESTROY;
    }
}
