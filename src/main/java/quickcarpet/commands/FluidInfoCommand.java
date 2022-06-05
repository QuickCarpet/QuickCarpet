package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.fluid.FluidState;
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
import static quickcarpet.utils.Constants.StateInfoCommand.Texts.FLUID_STATE;
import static quickcarpet.utils.Messenger.*;

public class FluidInfoCommand {
    public static final DynamicCommandExceptionType UNKNOWN_PROVIDER_EXCEPTION = new DynamicCommandExceptionType(
        id -> t("fluid_info_provider.unknown", id)
    );

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var fluidInfo = Utils.makeStateInfoCommand(
            literal("fluidinfo"),
            QuickCarpetRegistries.FLUID_INFO_PROVIDER,
            FluidInfoCommand::execute,
            FluidInfoCommand::executeDirection
        ).requires(source -> source.hasPermissionLevel(Settings.commandFluidInfo));

        dispatcher.register(fluidInfo);
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        ServerWorld world = source.getWorld();
        BlockPos pos = getLoadedBlockPos(ctx, "pos");
        FluidState state = world.getFluidState(pos);
        m(source, t(Keys.LINE, FLUID_STATE, format(state)));
        return Utils.executeStateInfo(source, pos, state, QuickCarpetRegistries.FLUID_INFO_PROVIDER);
    }

    private static int executeDirection(CommandContext<ServerCommandSource> ctx, Direction direction) throws CommandSyntaxException {
        return Utils.executeStateInfo(ctx, direction, QuickCarpetRegistries.FLUID_INFO_PROVIDER, BlockView::getFluidState, UNKNOWN_PROVIDER_EXCEPTION::create);
    }
}
