package quickcarpet.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.dedicated.ServerCommandOutput;
import quickcarpet.QuickCarpet;
import quickcarpet.mixin.accessor.ServerCommandSourceAccessor;

import static net.minecraft.server.command.CommandManager.literal;
import static quickcarpet.utils.Messenger.m;
import static quickcarpet.utils.Messenger.s;

public class TelemetryCommand {
    private static final Gson GSON_CONCISE = new GsonBuilder().disableHtmlEscaping().create();
    private static final Gson GSON_PRETTY = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("telemetry").requires(s -> s.hasPermissionLevel(4)).executes(TelemetryCommand::telemetry));
    }

    private static int telemetry(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        CommandOutput output = ((ServerCommandSourceAccessor) source).getOutput();
        boolean isConsole = output instanceof MinecraftServer || output instanceof ServerCommandOutput;
        Gson gson = isConsole ? GSON_CONCISE : GSON_PRETTY;
        JsonObject telemetry = QuickCarpet.getInstance().getTelemetryData();
        m(source, s(gson.toJson(telemetry)));
        return 0;
    }
}
