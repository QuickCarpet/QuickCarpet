package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import quickcarpet.Build;
import quickcarpet.QuickCarpet;
import quickcarpet.module.QuickCarpetModule;
import quickcarpet.settings.ParsedRule;
import quickcarpet.settings.RuleCategory;
import quickcarpet.settings.Settings;

import java.util.*;
import java.util.stream.Stream;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static quickcarpet.utils.Messenger.*;

public class CarpetCommand {
    private static final Set<String> RULE_CATEGORIES = new LinkedHashSet<>();

    static {
        for (RuleCategory c : RuleCategory.values()) RULE_CATEGORIES.add(c.lowerCase);
        for (QuickCarpetModule m : QuickCarpet.getInstance().modules) {
            if (Settings.MANAGER.getModuleSettings(m) != null) RULE_CATEGORIES.add(m.getId());
        }
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> carpet = literal("carpet").requires((player) ->
                player.hasPermissionLevel(2));

        carpet.executes((context)->listAllSettings(context.getSource())).
                then(literal("list").
                        executes( (c) -> listSettings(c.getSource(),
                                new TranslatableText("command.carpet.title.all", Build.NAME),
                                Settings.MANAGER.getRules())).
                        then(literal("defaults").
                                executes( (c)-> listSettings(c.getSource(),
                                        new TranslatableText("command.carpet.title.startup"),
                                        Settings.MANAGER.getSavedRules()))).
                        then(argument("search", StringArgumentType.word()).
                                suggests( (c, b)-> CommandSource.suggestMatching(RULE_CATEGORIES, b)).
                                executes( (c) -> listSettings(c.getSource(),
                                        new TranslatableText("command.carpet.title.search", Build.NAME, getString(c, "search")),
                                        Settings.MANAGER.getRulesMatching(getString(c, "search"))))));

        LiteralArgumentBuilder<ServerCommandSource> removeDefault = literal("removeDefault").requires(s -> !Settings.MANAGER.isLocked());
        LiteralArgumentBuilder<ServerCommandSource> setDefault = literal("setDefault").requires(s -> !Settings.MANAGER.isLocked());
        for (ParsedRule<?> rule : Settings.MANAGER.getRules()) {
            carpet.then(literal(rule.getName())
                .executes(c -> displayRuleMenu(c.getSource(), rule))
                .then(argument("value", rule.getArgumentType())
                    .suggests((c, b) -> CommandSource.suggestMatching(rule.getOptions(), b))
                    .requires(s -> !Settings.MANAGER.isLocked())
                    .executes(c -> setRule(c.getSource(), (ParsedRule) rule, rule.getArgument(c)))
                )
            );
            removeDefault.then(literal(rule.getName())
                    .requires(s -> rule.hasSavedValue())
                    .executes(c -> removeDefault(c.getSource(), rule)));
            setDefault.then(literal(rule.getName()).then(argument("value", rule.getArgumentType())
                .suggests((c, b) -> CommandSource.suggestMatching(rule.getOptions(), b))
                .executes(c -> setDefault(c.getSource(), (ParsedRule) rule, rule.getArgument(c)))
            ));
        }
        carpet.then(removeDefault);
        carpet.then(setDefault);
        dispatcher.register(carpet);
    }

    private static int displayRuleMenu(ServerCommandSource source, ParsedRule<?> rule) {
        if (!(source.getEntity() instanceof ServerPlayerEntity)) {
            m(source, t("command.carpet.rule.value", rule.getName(), s(rule.getAsString(), Formatting.BOLD)));
            return 1;
        }

        m(source, "");
        m(source, runCommand(s(rule.getName(), Formatting.BOLD), "/carpet" + rule.getName(), ts("command.carpet.refresh", Formatting.GRAY)));
        m(source, rule.getDescription());

        if (rule.getExtraInfo() != null) m(source, style(rule.getExtraInfo(), Formatting.GRAY));

        if (rule.getDeprecated() != null) m(source, ts("command.carpet.rule.deprecated", Formatting.RED, rule.getDeprecated()));

        List<Text> categories = new ArrayList<>();
        categories.add(t("command.carpet.categories"));
        Stream<String> ruleCategories = rule.getCategories().stream().map(c -> c.lowerCase);
        if (rule.getModule() != null) {
            ruleCategories = Stream.concat(Stream.of(rule.getModule().getId()), ruleCategories);
        }
        ruleCategories.forEach(name -> {
            categories.add(runCommand(s("[" + name + "]", Formatting.AQUA), "/carpet list " + name, t("command.carpet.list", name)));
            categories.add(s(", "));
        });
        categories.remove(categories.size()-1);
        m(source, categories.toArray(new Object[0]));

        Text valueText = s(rule.getAsString(), rule.getBoolValue() ? Formatting.GREEN : Formatting.DARK_RED, Formatting.BOLD);
        Text status = t(rule.isDefault() ? "carpet.rule.value.default" : "carpet.rule.value.modified");
        m(source, t("carpet.rule.currentValue", valueText, status));
        MutableText options = t("command.carpet.options");
        options.append(s("[", Formatting.YELLOW));
        boolean first = true;
        for (String o: rule.getOptions()) {
            if (first) first = false;
            else options.append(" ");
            options.append(makeSetRuleButton(rule, o, false));
        }
        options.append(s("]", Formatting.YELLOW));
        m(source, options);
        return 1;
    }

    private static Text makeSetRuleButton(ParsedRule<?> rule, String option, boolean brackets) {
        MutableText baseText = s((brackets ? "[" : "") + option + (brackets ? "]" : ""));
        if (option.equals(rule.getDefaultAsString()) && !brackets) baseText = baseText.formatted(Formatting.UNDERLINE);
        baseText = option.equals(rule.getAsString()) ? baseText.formatted(Formatting.BOLD, Formatting.GREEN) : baseText.formatted(Formatting.YELLOW);
        if (Settings.MANAGER.isLocked()) {
            return hoverText(baseText, ts("carpet.locked", Formatting.GRAY));
        }
        return suggestCommand(baseText, "/carpet " + rule.getName() + " " + option, ts("command.carpet.switch", Formatting.GRAY, option));
    }

    private static <T> int setRule(ServerCommandSource source, ParsedRule<T> rule, T newValue) {
        try {
            rule.set(newValue, true);
            MutableText changePermanently = c(s("["), t("command.carpet.changePermanently"), s("]"));
            style(changePermanently, Formatting.AQUA);
            hoverText(changePermanently, t("command.carpet.changePermanently.hover"));
            suggestCommand(changePermanently, "/carpet setDefault " + rule.getName() + " " + rule.getAsString());
            m(source, s(rule.toString() + " "), changePermanently);
        } catch (ParsedRule.ValueException e) {
            throw new CommandException(ts("command.carpet.rule.invalidValue", Formatting.RED, e.message));
        } catch (IllegalArgumentException e) {
            throw new CommandException(ts("command.carpet.rule.invalidValue", Formatting.RED, e.getMessage()));
        }
        return 1;
    }

    private static <T> int setDefault(ServerCommandSource source, ParsedRule<T> rule, T defaultValue) {
        try {
            rule.set(defaultValue, true);
            rule.save();
            m(source, ts("command.carpet.setDefault.success", GRAY_ITALIC, rule.getName(), defaultValue));
        } catch (ParsedRule.ValueException e) {
            throw new CommandException(ts("command.carpet.rule.invalidValue", Formatting.RED, e.message));
        }
        return 1;
    }

    private static int removeDefault(ServerCommandSource source, ParsedRule<?> rule) {
        rule.resetToDefault(true);
        rule.save();
        m(source, ts("command.carpet.removeDefault.success", GRAY_ITALIC, rule.getName(), rule.getAsString()));
        return 1;
    }

    private static Text displayInteractiveSetting(ParsedRule<?> rule) {
        MutableText text = s("- " + rule.getName());
        runCommand(text, "/carpet " + rule.getName(), style(rule.getDescription().copy(), Formatting.YELLOW));
        boolean first = true;
        for (String option : rule.getOptions()) {
            if (first) {
                text.append(" ");
                first = false;
            }
            text.append(makeSetRuleButton(rule, option, true));
        }
        return text;
    }

    private static int listSettings(ServerCommandSource source, Text title, Iterable<ParsedRule<?>> rules) {
        if (source.getEntity() instanceof ServerPlayerEntity) {
            title.getStyle().withFormatting(Formatting.BOLD);
            m(source, title);
            for (ParsedRule<?> rule : rules) {
                m(source, displayInteractiveSetting(rule));
            }
        } else {
            m(source, title);
            for (ParsedRule<?> rule : rules) {
                m(source, s("- " + rule.toString()));
            }
        }
        return 1;
    }

    private static int listAllSettings(ServerCommandSource source) {
        listSettings(source, new TranslatableText("command.carpet.title.current", Build.NAME), Settings.MANAGER.getNonDefault());

        m(source, s(""));
        String version = Build.VERSION;
        if (QuickCarpet.isDevelopment()) {
            version += " " + Build.BRANCH + "-" + Build.COMMIT.substring(0, 7) + " (" + Build.BUILD_TIMESTAMP + ")";
        }
        m(source, ts("command.carpet.version", Formatting.DARK_GREEN, Build.NAME, version));
        if (!Build.MINECRAFT_VERSION.equals(SharedConstants.getGameVersion().getId())) {
            m(source, ts("command.carpet.version.builtFor", Formatting.YELLOW, Build.MINECRAFT_VERSION));
        }
        if (!Build.WORKING_DIR_CLEAN) {
            m(source, ts("command.carpet.version.uncommitted", Formatting.RED));
        }
        m(source, s(""));
        Collection<QuickCarpetModule> modules = QuickCarpet.getInstance().modules;
        if (!modules.isEmpty()) {
            m(source, ts("command.carpet.modules", Formatting.BOLD));
            for (QuickCarpetModule m : modules) {
                MutableText button = s("[" + m.getId() + "]", Formatting.AQUA);
                runCommand(button, "/carpet list " + m.getId(), ts("command.carpet.modules.list", Formatting.GRAY));
                m(source, button, s(" " + m.getName() + " " + m.getVersion()));
            }
        }
        m(source, s(""));
        if (source.getEntity() instanceof ServerPlayerEntity) {
            MutableText categories = ts("command.carpet.browseCategories", Formatting.BOLD);
            boolean first = true;
            for (String name : RULE_CATEGORIES) {
                if (first)  first = false;
                else categories.append(" ");
                categories.append(runCommand(s("[" + name + "]", Formatting.AQUA), "/carpet list " + name, ts("command.carpet.list", Formatting.GRAY, name)));
            }
            m(source, categories);
        }
        return 1;
    }
}
