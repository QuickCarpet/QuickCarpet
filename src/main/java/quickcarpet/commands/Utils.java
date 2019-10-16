package quickcarpet.commands;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;

public class Utils {
    static <T> T getOrNull(CommandContext<ServerCommandSource> context, String argument, Class<T> type) {
        try {
            return context.getArgument(argument, type);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().startsWith("No such argument")) return null;
            throw e;
        }
    }

    static <T> T getOrDefault(CommandContext<ServerCommandSource> context, String argument, T defaultValue) {
        T value = getOrNull(context, argument, (Class<T>) defaultValue.getClass());
        return value == null ? defaultValue : value;
    }
}
