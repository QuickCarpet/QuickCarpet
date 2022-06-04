package quickcarpet.utils;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import quickcarpet.commands.MeasureCommand;
import quickcarpet.commands.SpawnCommand;
import quickcarpet.commands.Utils;
import quickcarpet.helper.HopperCounter;
import quickcarpet.helper.WoolTool;
import quickcarpet.settings.Settings;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static quickcarpet.utils.Constants.StateInfoCommand.Texts.BLOCK_STATE;
import static quickcarpet.utils.Constants.StateInfoCommand.Texts.FLUID_STATE;
import static quickcarpet.utils.Messenger.*;

public final class Carpets {
    private static final Map<UUID, Vec3d> MEASURE_START_POINTS = new HashMap<>();

    private Carpets() {}

    public static boolean onPlace(@Nullable PlayerEntity player, World world, BlockPos pos, DyeColor color, Vec3d hitPos) {
        if (!Settings.carpets || player == null) return false;
        var action = getAction(player, color);
        if (action == null) return false;
        if (player instanceof ServerPlayerEntity serverPlayer && world instanceof ServerWorld serverWorld) {
            action.execute(serverPlayer, serverWorld, pos, hitPos);
        }
        return true;
    }

    private static CarpetAction getAction(PlayerEntity player, DyeColor color) {
        return switch (color) {
            case PINK -> canExecuteCommand(player, Settings.commandSpawn) ? Carpets::spawnList : null;
            case BLACK -> canExecuteCommand(player, Settings.commandSpawn) ? Carpets::spawnMobcaps : null;
            case GRAY -> canExecuteCommand(player, Settings.commandBlockInfo) ? Carpets::blockInfo : null;
            case BLUE -> canExecuteCommand(player, Settings.commandFluidInfo) ? Carpets::fluidInfo : null;
            case BROWN -> canExecuteCommand(player, Settings.commandMeasure) ? Carpets::measure : null;
            case GREEN -> Settings.hopperCounters ? Carpets::counterValue : null;
            case RED -> Settings.hopperCounters ? Carpets::counterReset : null;
            default -> null;
        };
    }

    private static void spawnList(ServerPlayerEntity player, ServerWorld world, BlockPos pos, Vec3d hitPos) {
        SpawnCommand.list(player.getCommandSource(), pos);
    }

    private static void spawnMobcaps(ServerPlayerEntity player, ServerWorld world, BlockPos pos, Vec3d hitPos) {
        SpawnCommand.sendMobcaps(player.getCommandSource(), world);
    }

    private static void blockInfo(ServerPlayerEntity player, ServerWorld world, BlockPos pos, Vec3d hitPos) {
        var source = player.getCommandSource();
        var targetPos = pos.down();
        var state = world.getBlockState(targetPos);
        m(source, t(Constants.StateInfoCommand.Keys.LINE, BLOCK_STATE, format(state)));
        Utils.executeStateInfo(source, targetPos, state, QuickCarpetRegistries.BLOCK_INFO_PROVIDER);
    }

    private static void fluidInfo(ServerPlayerEntity player, ServerWorld world, BlockPos pos, Vec3d hitPos) {
        var source = player.getCommandSource();
        var state = world.getFluidState(pos);
        m(source, t(Constants.StateInfoCommand.Keys.LINE, FLUID_STATE, format(state)));
        Utils.executeStateInfo(source, pos, state, QuickCarpetRegistries.FLUID_INFO_PROVIDER);
    }

    private static void measure(ServerPlayerEntity player, ServerWorld world, BlockPos pos, Vec3d hitPos) {
        var uuid = player.getUuid();
        var startPoint = MEASURE_START_POINTS.remove(uuid);
        if (startPoint == null || player.isSneaking()) {
            MEASURE_START_POINTS.put(uuid, hitPos);
            return;
        }
        MeasureCommand.measure(player.getCommandSource(), startPoint, hitPos);
    }

    private static void counterValue(ServerPlayerEntity player, ServerWorld world, BlockPos pos, Vec3d hitPos) {
        var counter = HopperCounter.getCounter(getCounterKey(world, pos));
        send(player.getCommandSource(), counter.format(world.getServer(), false, false));
    }

    private static void counterReset(ServerPlayerEntity player, ServerWorld world, BlockPos pos, Vec3d hitPos) {
        var counter = HopperCounter.getCounter(getCounterKey(world, pos));
        counter.reset(world.getServer());
        m(player.getCommandSource(), t(Constants.CounterCommand.Keys.RESET_ONE_SUCCESS, counter.key.getText()));
    }

    private static HopperCounter.Key getCounterKey(ServerWorld world, BlockPos pos) {
        var targetPos = pos.down();
        var woolColor = WoolTool.getCounterKey(world, targetPos);
        if (woolColor != null) return woolColor;
        var state = world.getBlockState(targetPos);
        return state.isOf(Blocks.CACTUS) ? HopperCounter.Key.CACTUS : HopperCounter.Key.ALL;
    }

    private static boolean canExecuteCommand(PlayerEntity player, int level) {
        return player.hasPermissionLevel(level);
    }

    interface CarpetAction {
        void execute(ServerPlayerEntity player, ServerWorld world, BlockPos pos, Vec3d hitPos);
    }
}
