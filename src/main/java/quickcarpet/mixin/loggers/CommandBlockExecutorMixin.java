package quickcarpet.mixin.loggers;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.CommandBlockExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.QuickCarpetServer;
import quickcarpet.commands.Utils;
import quickcarpet.logging.Loggers;
import quickcarpet.logging.loghelpers.LogParameter;

import java.util.Arrays;

import static quickcarpet.utils.Messenger.*;

@Mixin(CommandBlockExecutor.class)
public class CommandBlockExecutorMixin {
    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/CommandManager;execute(Lnet/minecraft/server/command/ServerCommandSource;Ljava/lang/String;)I"))
    private int logExecute(CommandManager manager, ServerCommandSource source, String command) {
        int result = manager.execute(source, command);
        if (Loggers.COMMAND_BLOCKS.isActive()) log(source, command, result);
        return result;
    }

    private static void log(ServerCommandSource source, String command, int result) {
        Vec3d pos = source.getPosition();
        BlockPos blockPos = new BlockPos(pos);
        Vec3d center = Vec3d.ofCenter(blockPos);
        String commandWithoutSlash = command.startsWith("/") ? command.substring(1) : command;
        Loggers.COMMAND_BLOCKS.log(option -> {
            boolean isCentered = pos.squaredDistanceTo(center) < 0.01;
            switch (option) {
                case "brief":
                    return isCentered ? tp(blockPos, Formatting.AQUA) : tp(pos, Formatting.AQUA);
                case "full":
                    CommandDispatcher<ServerCommandSource> dispatcher = QuickCarpetServer.getMinecraftServer().getCommandManager().getDispatcher();
                    ParseResults<ServerCommandSource> parsed = dispatcher.parse(commandWithoutSlash, source);
                    MutableText highlightedCommand = parsed.getReader().canRead() ? s(commandWithoutSlash, Formatting.RED) : Utils.highlight(parsed, commandWithoutSlash, 0);
                    return c(
                        isCentered ? tp(blockPos, Formatting.AQUA) : tp(pos, Formatting.AQUA),
                        s(" ", Formatting.WHITE),
                        highlightedCommand,
                        s(" = ", Formatting.GRAY),
                        s(Integer.toString(result), Formatting.GREEN)
                    );
            }
            return null;
        }, () -> Arrays.asList(
            new LogParameter("x", pos.x),
            new LogParameter("y", pos.y),
            new LogParameter("z", pos.z),
            new LogParameter("block.x", blockPos.getX()),
            new LogParameter("block.y", blockPos.getY()),
            new LogParameter("block.z", blockPos.getZ()),
            new LogParameter("command", command),
            new LogParameter("result", result)
        ));
    }
}
