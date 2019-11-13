package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import quickcarpet.QuickCarpet;
import quickcarpet.logging.*;
import quickcarpet.settings.Settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static quickcarpet.utils.Messenger.*;

public class LogCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> log = literal("log")
            .requires(s -> s.hasPermissionLevel(Settings.commandLog))
            .executes((context) -> listLogs(context.getSource()))
            .then(literal("clear")
                .executes(c -> unsubFromAll(c.getSource(), c.getSource().getName()))
                .then(argument("player", StringArgumentType.word()).requires(s -> s.hasPermissionLevel(2))
                    .suggests((c, b)-> CommandSource.suggestMatching(c.getSource().getPlayerNames(), b))
                    .executes(c -> unsubFromAll(c.getSource(), getString(c, "player")))));

        LiteralArgumentBuilder<ServerCommandSource> handlerArg = literal("handler");
        for (Map.Entry<String, LogHandler.LogHandlerCreator> c : LogHandlers.CREATORS.entrySet()) {
            LiteralArgumentBuilder<ServerCommandSource> handler = literal(c.getKey());
            if (c.getValue().usesExtraArgs()) {
                handlerArg.then(handler
                        .executes(ctx -> subscribe(ctx, c.getKey()))
                        .then(argument("extra", greedyString()).executes(ctx -> subscribe(ctx, c.getKey()))));
            } else {
                handlerArg.then(handler.executes(ctx -> subscribe(ctx, c.getKey())));
            }
        }

        LiteralArgumentBuilder<ServerCommandSource> playerArg = literal("player").requires(s -> s.hasPermissionLevel(2))
                .then(argument("player", StringArgumentType.word())
                .suggests( (c, b) -> CommandSource.suggestMatching(c.getSource().getPlayerNames(),b))
                .executes(LogCommand::subscribe)
                    .then(handlerArg));

        log.then(argument("log name", StringArgumentType.word())
            .suggests((c, b)-> CommandSource.suggestMatching(Loggers.getLoggerNames(),b))
            .executes(c -> toggleSubscription(c.getSource(), c.getSource().getName(), getString(c, "log name")))
            .then(literal("clear")
                .executes( (c) -> unsubFromLogger(
                    c.getSource(),
                    c.getSource().getName(),
                    getString(c, "log name"))))
            .then(playerArg)
            .then(handlerArg)
            .then(argument("option", StringArgumentType.word())
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
        return CommandSource.suggestMatching(options, builder);
    }

    private static int listLogs(ServerCommandSource source) {
        PlayerEntity player;
        try {
            player = source.getPlayer();
        } catch (CommandSyntaxException e) {
            m(source, ts("command.log.playerOnly", RED));
            return 0;
        }
        LoggerManager.PlayerSubscriptions subs = QuickCarpet.getInstance().loggers.getPlayerSubscriptions(source.getName());
        List<Logger<?>> loggers = new ArrayList<>(Loggers.values());
        Collections.sort(loggers);
        m(player, s("_____________________"));
        m(player, t("command.log.availableOptions"));
        for (Logger<?> logger : loggers) {
            boolean subscribed = subs.isSubscribedTo(logger);
            Text line = s(" - " + logger.getName() + ": ");
            String[] options = logger.getOptions();
            if (options.length == 0) {
                if (subscribed) {
                    line.append(ts("command.log.subscribed", LIME));
                } else {
                    Text button = style(c(s("["), t("command.log.action.subscribe"), s("]")), GRAY);
                    runCommand(button, "/log " + logger.getName(), t("command.log.action.subscribeTo", logger.getName()));
                    line.append(button);
                }
            } else {
                for (String option : logger.getOptions()) {
                    if (subscribed && option.equalsIgnoreCase(subs.getOption(logger))) {
                        line.append(s("[" + option + "]", LIME));
                    } else {
                        Text button = style(c(s("[" + option + "]")), GRAY);
                        Text hoverText = t("command.log.action.subscribeTo.option", logger.getName(), option);
                        runCommand(button, "/log " + logger.getName() + " " + option, hoverText);
                        line.append(button);
                    }
                }
            }
            m(player, line);
        }
        return 1;
    }

    private static boolean areArgumentsInvalid(ServerCommandSource source, String playerName, String loggerName) {
        PlayerEntity player = source.getMinecraftServer().getPlayerManager().getPlayer(playerName);
        if (player == null) {
            m(source, ts("command.log.noPlayerSpecified", RED));
            return true;
        }
        if (loggerName != null && Loggers.getLogger(loggerName) == null) {
            Logger logger = Loggers.getLogger(loggerName, true);
            if (logger != null) {
                m(source, ts("command.log.unavailable", RED, style(logger.getUnavailabilityReason(), "db")));
            } else {
                m(source, ts("command.log.unknown", RED, s(loggerName, BOLD)));
            }
            return true;
        }
        return false;
    }

    private static int unsubFromAll(ServerCommandSource source, String playerName) {
        if (areArgumentsInvalid(source, playerName, null)) return 0;
        for (String loggerName : Loggers.getLoggerNames()) {
            QuickCarpet.getInstance().loggers.unsubscribePlayer(playerName, loggerName);
        }
        m(source, ts("command.log.unsubscribed.all", GRAY + "" + ITALIC));
        return 1;
    }

    private static int unsubFromLogger(ServerCommandSource source, String playerName, String loggerName) {
        if (areArgumentsInvalid(source, playerName, loggerName)) return 0;
        QuickCarpet.getInstance().loggers.unsubscribePlayer(playerName, loggerName);
        m(source, ts("command.log.unsubscribed", GRAY + "" + ITALIC, loggerName));
        return 1;
    }

    private static int toggleSubscription(ServerCommandSource source, String playerName, String loggerName) {
        if (areArgumentsInvalid(source, playerName, loggerName)) return 0;
        boolean subscribed = QuickCarpet.getInstance().loggers.togglePlayerSubscription(playerName, loggerName, null);
        if (subscribed) {
            if (playerName.equalsIgnoreCase(source.getName())) {
                m(source, ts("command.log.subscribedTo", GRAY + "" + ITALIC, loggerName));
            } else {
                m(source, ts("command.log.subscribedTo.player", GRAY + "" + ITALIC, playerName, loggerName));
            }
        } else {
            if (playerName.equalsIgnoreCase(source.getName())) {
                m(source, ts("command.log.unsubscribed", GRAY + "" + ITALIC, loggerName));
            } else {
                m(source, ts("command.log.unsubscribed.player", GRAY + "" + ITALIC, playerName, loggerName));
            }
        }
        return 1;
    }

    private static int subscribePlayer(ServerCommandSource source, String playerName, String loggerName, String option, LogHandler handler) {
        if (areArgumentsInvalid(source, playerName, loggerName)) return 0;
        QuickCarpet.getInstance().loggers.subscribePlayer(playerName, loggerName, option, handler);
        if (option != null) {
            if (playerName.equalsIgnoreCase(source.getName())) {
                m(source, ts("command.log.subscribedTo.option", GRAY + "" + ITALIC, loggerName, option));
            } else {
                m(source, ts("command.log.subscribedTo.option.player", GRAY + "" + ITALIC, playerName, loggerName, option));
            }
        } else {
            if (playerName.equalsIgnoreCase(source.getName())) {
                m(source, ts("command.log.subscribedTo", GRAY + "" + ITALIC, loggerName));
            } else {
                m(source, ts("command.log.subscribedTo.player", GRAY + "" + ITALIC, playerName, loggerName));
            }
        }
        return 1;
    }
}
