package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import quickcarpet.QuickCarpetServer;
import quickcarpet.logging.*;
import quickcarpet.settings.Settings;
import quickcarpet.utils.Constants.LogCommand.Keys;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.minecraft.command.CommandSource.suggestMatching;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static quickcarpet.utils.Constants.LogCommand.Texts.*;
import static quickcarpet.utils.Messenger.*;

public class LogCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var log = literal("log")
            .requires(s -> s.hasPermissionLevel(Settings.commandLog))
            .executes(c -> listLogs(c.getSource()))
            .then(literal("clear")
                .executes(c -> unsubFromAll(c.getSource(), c.getSource().getName()))
                .then(argument("player", word()).requires(s -> s.hasPermissionLevel(2))
                    .suggests((c, b)-> suggestMatching(c.getSource().getPlayerNames(), b))
                    .executes(c -> unsubFromAll(c.getSource(), getString(c, "player")))));

        var handlerArg = literal("handler");
        for (Map.Entry<String, LogHandler.LogHandlerCreator> c : LogHandlers.CREATORS.entrySet()) {
            String name = c.getKey();
            var handler = literal(name);
            if (c.getValue().usesExtraArgs()) {
                handlerArg.then(handler
                    .executes(ctx -> subscribe(ctx, name))
                    .then(argument("extra", greedyString()).executes(ctx -> subscribe(ctx, name))));
            } else {
                handlerArg.then(handler.executes(ctx -> subscribe(ctx, name)));
            }
        }

        var playerArg = literal("player")
            .requires(s -> s.hasPermissionLevel(2))
            .then(argument("player", word())
            .suggests((c, b) -> suggestMatching(c.getSource().getPlayerNames(), b))
            .executes(LogCommand::subscribe)
                .then(handlerArg));

        log.then(argument("log name", word())
            .suggests((c, b)-> suggestMatching(Loggers.getLoggerNames(), b))
            .executes(c -> toggleSubscription(c.getSource(), c.getSource().getName(), getString(c, "log name")))
            .then(literal("clear")
                .executes(c -> unsubFromLogger(
                    c.getSource(),
                    c.getSource().getName(),
                    getString(c, "log name"))))
            .then(playerArg)
            .then(handlerArg)
            .then(argument("option", word())
                .suggests(LogCommand::suggestLoggerOptions)
                .executes(LogCommand::subscribe)
                .then(playerArg)));

        dispatcher.register(log);
    }

    private static int subscribe(CommandContext<ServerCommandSource> context, String handlerName) {
        String player = Utils.getOrDefault(context, "player", context.getSource().getName());
        String logger = getString(context, "log name");
        String option = Utils.getOrNull(context, "option", String.class);
        LogHandler handler = null;
        if (handlerName != null) {
            String extra = Utils.getOrNull(context, "extra", String.class);
            String[] extraArgs = extra == null ? new String[0] : extra.split(" ");
            handler = LogHandlers.createHandler(handlerName, extraArgs);
        }
        return subscribePlayer(context.getSource(), player, logger, option, handler);
    }

    private static int subscribe(CommandContext<ServerCommandSource> context) {
        return subscribe(context, null);
    }

    private static CompletableFuture<Suggestions> suggestLoggerOptions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        String loggerName = getString(context, "log name");
        Logger logger = Loggers.getLogger(loggerName);
        String[] options = logger == null ? new String[]{} : logger.getOptions();
        return suggestMatching(options, builder);
    }

    private static int listLogs(ServerCommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity)) {
            m(source, PLAYER_ONLY);
            return 0;
        }
        LoggerManager.PlayerSubscriptions subs = getLoggers().getPlayerSubscriptions(source.getName());
        List<Logger> loggers = new ArrayList<>(Loggers.values());
        Collections.sort(loggers);
        m(source, s("_____________________"));
        m(source, AVAILABLE_OPTIONS);
        for (Logger logger : loggers) {
            m(source, formatListEntry(subs, logger, subs.isSubscribedTo(logger)));
        }
        return 1;
    }

    private static MutableText formatListEntry(LoggerManager.PlayerSubscriptions subs, Logger logger, boolean subscribed) {
        MutableText line = s(" - " + logger.getName() + ": ");
        String[] options = logger.getOptions();
        if (options.length == 0) {
            if (subscribed) {
                line.append(SUBSCRIBED);
            } else {
                line.append(formatButton(
                    ACTION_SUBSCRIBE,
                    t(Keys.ACTION_SUBSCRIBE_TO, logger.getName()),
                    "/log " + logger.getName(),
                    true
                ));
            }
        } else {
            for (String option : logger.getOptions()) {
                line.append(formatButton(
                    s(option),
                    t(Keys.ACTION_SUBSCRIBE_TO_OPTION, logger.getName(), option),
                    "/log " + logger.getName() + " " + option,
                    !subscribed || !option.equalsIgnoreCase(subs.getOption(logger)
                )));
            }
        }
        return line;
    }

    private static MutableText formatButton(Text buttonText, Text hoverText, String command, boolean active) {
        MutableText button = c(s("["), buttonText, s("]"));
        if (active) {
            style(button, Formatting.GRAY);
            runCommand(button, command, hoverText);
        } else {
            style(button, Formatting.AQUA);
        }
        return button;
    }

    private static boolean areArgumentsInvalid(ServerCommandSource source, String playerName, String loggerName) {
        PlayerEntity player = source.getServer().getPlayerManager().getPlayer(playerName);
        if (player == null) {
            m(source, NO_PLAYER_SPECIFIED);
            return true;
        }
        if (loggerName != null && Loggers.getLogger(loggerName) == null) {
            Logger logger = Loggers.getLogger(loggerName, true);
            if (logger != null) {
                MutableText reason = logger.getUnavailabilityReason();
                assert reason != null;
                m(source, ts(Keys.UNAVAILABLE, Formatting.RED, style(reason, Formatting.GOLD, Formatting.BOLD)));
            } else {
                m(source, ts(Keys.UNKNOWN, Formatting.RED, s(loggerName, Formatting.BOLD)));
            }
            return true;
        }
        return false;
    }

    private static int unsubFromAll(ServerCommandSource source, String playerName) {
        if (areArgumentsInvalid(source, playerName, null)) return 0;
        for (String loggerName : Loggers.getLoggerNames()) {
            getLoggers().unsubscribePlayer(playerName, loggerName);
        }
        m(source, UNSUBSCRIBED_ALL);
        return 1;
    }

    private static int unsubFromLogger(ServerCommandSource source, String playerName, String loggerName) {
        if (areArgumentsInvalid(source, playerName, loggerName)) return 0;
        getLoggers().unsubscribePlayer(playerName, loggerName);
        m(source, ts(Keys.UNSUBSCRIBED, GRAY_ITALIC, loggerName));
        return 1;
    }

    private static int toggleSubscription(ServerCommandSource source, String playerName, String loggerName) {
        if (areArgumentsInvalid(source, playerName, loggerName)) return 0;
        boolean subscribed = getLoggers().togglePlayerSubscription(playerName, loggerName, null);
        if (subscribed) {
            if (playerName.equalsIgnoreCase(source.getName())) {
                m(source, ts(Keys.SUBSCRIBED_TO, GRAY_ITALIC, loggerName));
            } else {
                m(source, ts(Keys.SUBSCRIBED_TO_PLAYER, GRAY_ITALIC, playerName, loggerName));
            }
        } else {
            if (playerName.equalsIgnoreCase(source.getName())) {
                m(source, ts(Keys.UNSUBSCRIBED, GRAY_ITALIC, loggerName));
            } else {
                m(source, ts(Keys.UNSUBSCRIBED_PLAYER, GRAY_ITALIC, playerName, loggerName));
            }
        }
        return 1;
    }

    private static int subscribePlayer(ServerCommandSource source, String playerName, String loggerName, String option, LogHandler handler) {
        if (areArgumentsInvalid(source, playerName, loggerName)) return 0;
        getLoggers().subscribePlayer(playerName, loggerName, option, handler);
        if (option != null) {
            if (playerName.equalsIgnoreCase(source.getName())) {
                m(source, ts(Keys.SUBSCRIBED_TO_OPTION, GRAY_ITALIC, loggerName, option));
            } else {
                m(source, ts(Keys.SUBSCRIBED_TO_OPTION_PLAYER, GRAY_ITALIC, playerName, loggerName, option));
            }
        } else {
            if (playerName.equalsIgnoreCase(source.getName())) {
                m(source, ts(Keys.SUBSCRIBED_TO, GRAY_ITALIC, loggerName));
            } else {
                m(source, ts(Keys.SUBSCRIBED_TO_PLAYER, GRAY_ITALIC, playerName, loggerName));
            }
        }
        return 1;
    }

    private static LoggerManager getLoggers() {
        return QuickCarpetServer.getInstance().loggers;
    }
}
