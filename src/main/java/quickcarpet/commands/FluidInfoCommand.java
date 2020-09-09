package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import quickcarpet.helper.FluidInfoProvider;
import quickcarpet.settings.Settings;

import static net.minecraft.command.argument.BlockPosArgumentType.getLoadedBlockPos;
import static net.minecraft.server.command.CommandManager.literal;
import static quickcarpet.utils.Messenger.*;

public class FluidInfoCommand {
    public static final DynamicCommandExceptionType UNKNOWN_PROVIDER_EXCEPTION = new DynamicCommandExceptionType(
        id -> new TranslatableText("fluid_info_provider.unknown", id)
    );

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> fluidInfo = Utils.makeStateInfoCommand(
            literal("fluidinfo"),
            FluidInfoProvider.REGISTRY,
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
        m(source, t("command.stateinfo.line", t("command.stateinfo.fluid_state"), format(state)));
        return Utils.executeStateInfo(source, pos, state, FluidInfoProvider.REGISTRY);
    }

    private static int executeDirection(CommandContext<ServerCommandSource> ctx, Direction direction) throws CommandSyntaxException {
        return Utils.executeStateInfo(ctx, direction, FluidInfoProvider.REGISTRY, BlockView::getFluidState, UNKNOWN_PROVIDER_EXCEPTION::create);
    }
}
