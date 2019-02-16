package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.sun.istack.internal.Nullable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.class_3829;
import net.minecraft.command.arguments.BlockArgument;
import net.minecraft.command.arguments.BlockArgumentType;
import net.minecraft.command.arguments.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableIntBoundingBox;
import quickcarpet.QuickCarpetSettings;

import java.util.function.Predicate;

public class CarpetSetBlockCommand {
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableTextComponent("commands.setblock.failed", new Object[0]));

    public static void register(CommandDispatcher<ServerCommandSource> commandDispatcher_1) {
        commandDispatcher_1.register((LiteralArgumentBuilder)((LiteralArgumentBuilder) ServerCommandManager.literal("carpetsetblock").requires((player) ->
                QuickCarpetSettings.getBool("")
        )).then(ServerCommandManager.argument("pos", BlockPosArgumentType.create()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)ServerCommandManager.argument("block", BlockArgumentType.create()).executes((commandContext_1) -> {
            return method_13620((ServerCommandSource)commandContext_1.getSource(), BlockPosArgumentType.getValidPosArgument(commandContext_1, "pos"), BlockArgumentType.method_9655(commandContext_1, "block"), CarpetSetBlockCommand.class_3121.REPLACE, (Predicate)null);
        })).then(ServerCommandManager.literal("destroy").executes((commandContext_1) -> {
            return method_13620((ServerCommandSource)commandContext_1.getSource(), BlockPosArgumentType.getValidPosArgument(commandContext_1, "pos"), BlockArgumentType.method_9655(commandContext_1, "block"), CarpetSetBlockCommand.class_3121.DESTROY, (Predicate)null);
        }))).then(ServerCommandManager.literal("keep").executes((commandContext_1) -> {
            return method_13620((ServerCommandSource)commandContext_1.getSource(), BlockPosArgumentType.getValidPosArgument(commandContext_1, "pos"), BlockArgumentType.method_9655(commandContext_1, "block"), CarpetSetBlockCommand.class_3121.REPLACE, (cachedBlockPosition_1) -> {
                return cachedBlockPosition_1.getWorld().isAir(cachedBlockPosition_1.getBlockPos());
            });
        }))).then(ServerCommandManager.literal("replace").executes((commandContext_1) -> {
            return method_13620((ServerCommandSource)commandContext_1.getSource(), BlockPosArgumentType.getValidPosArgument(commandContext_1, "pos"), BlockArgumentType.method_9655(commandContext_1, "block"), CarpetSetBlockCommand.class_3121.REPLACE, (Predicate)null);
        })))));
    }

    private static int method_13620(ServerCommandSource serverCommandSource_1, BlockPos blockPos_1, BlockArgument blockArgument_1, CarpetSetBlockCommand.class_3121 setBlockCommand$class_3121_1, @Nullable Predicate<CachedBlockPosition> predicate_1) throws CommandSyntaxException {
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
                class_3829.method_16825(blockEntity_1);
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
        @Nullable
        BlockArgument filter(MutableIntBoundingBox var1, BlockPos var2, BlockArgument var3, ServerWorld var4);
    }

    enum class_3121 {
        REPLACE,
        DESTROY;
    }
}
