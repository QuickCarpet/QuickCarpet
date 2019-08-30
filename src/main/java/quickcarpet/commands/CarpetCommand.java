package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.SharedConstants;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
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

        LiteralArgumentBuilder<ServerCommandSource> removeDefault = literal("removeDefault").requires(s -> !Settings.MANAGER.locked);
        LiteralArgumentBuilder<ServerCommandSource> setDefault = literal("setDefault").requires(s -> !Settings.MANAGER.locked);
        for (ParsedRule rule : Settings.MANAGER.getRules()) {
            carpet.then(literal(rule.name)
                .executes(c -> displayRuleMenu(c.getSource(), rule))
                .then(argument("value", rule.getArgumentType())
                    .suggests((c, b) -> CommandSource.suggestMatching(rule.options, b))
                    .requires(s -> !Settings.MANAGER.locked)
                    .executes(c -> setRule((ServerCommandSource) c.getSource(), rule, rule.getArgument(c)))
                )
            );
            removeDefault.then(literal(rule.name)
                    .requires(s -> rule.hasSavedValue())
                    .executes(c -> removeDefault(c.getSource(), rule)));
            setDefault.then(literal(rule.name).then(argument("value", rule.getArgumentType())
                .suggests((c, b) -> CommandSource.suggestMatching(rule.options, b))
                .executes(c -> setDefault((ServerCommandSource) c.getSource(), rule, rule.getArgument(c)))
            ));
        }
        carpet.then(removeDefault);
        carpet.then(setDefault);
        dispatcher.register(carpet);
    }

    private static int displayRuleMenu(ServerCommandSource source, ParsedRule<?> rule) {
        PlayerEntity player;
        try {
            player = source.getPlayer();
        } catch (CommandSyntaxException e) {
            m(source, t("command.carpet.rule.value", rule.name, s(rule.getAsString(), BOLD)));
            return 1;
        }

        m(player, "");
        m(player, runCommand(s(rule.name, BOLD), "/carpet" + rule.name, ts("command.carpet.refresh", GRAY)));
        m(player, rule.description);

        if (rule.extraInfo != null) m(player, style(rule.extraInfo, GRAY));

        List<Text> categories = new ArrayList<>();
        categories.add(t("command.carpet.categories"));
        Stream<String> ruleCategories = rule.categories.stream().map(c -> c.lowerCase);
        if (rule.getModule() != null) {
            ruleCategories = Stream.concat(Stream.of(rule.getModule().getId()), ruleCategories);
        }
        ruleCategories.forEach(name -> {
            categories.add(runCommand(s("[" + name + "]", CYAN), "/carpet list " + name, t("command.carpet.list", name)));
            categories.add(s(", "));
        });
        categories.remove(categories.size()-1);
        m(player, categories.toArray(new Object[0]));

        Text valueText = s(rule.getAsString(), rule.getBoolValue() ? "lb" : "nb");
        Text status = t(rule.isDefault() ? "carpet.rule.value.default" : "carpet.rule.value.modified");
        m(player, t("carpet.rule.currentValue", valueText, status));
        Text options = t("command.carpet.options");
        options.append(s("[", YELLOW));
        boolean first = true;
        for (String o: rule.options) {
            if (first) first = false;
            else options.append(" ");
            options.append(makeSetRuleButton(rule, o, false));
        }
        options.append(s("]", YELLOW));
        m(player, options);
        return 1;
    }

    private static Text makeSetRuleButton(ParsedRule<?> rule, String option, boolean brackets) {
        String color = "";
        if (option.equals(rule.defaultAsString) && !brackets) color += UNDERLINE;
        color += option.equals(rule.getAsString()) ? "bl" : "y";
        Text baseText = s((brackets ? "[" : "") + option + (brackets ? "]" : ""), color);
        if (Settings.MANAGER.locked) {
            return hoverText(baseText, ts("carpet.locked", GRAY));
        }
        return suggestCommand(baseText, "/carpet " + rule.name + " " + option, ts("command.carpet.switch", GRAY, option));
    }

    private static <T> int setRule(ServerCommandSource source, ParsedRule<T> rule, T newValue) {
        try {
            rule.set(newValue, true);
            Text changePermanently = c(s("["), t("command.carpet.changePermanently"), s("]"));
            style(changePermanently, CYAN);
            hoverText(changePermanently, t("command.carpet.changePermanently.hover"));
            suggestCommand(changePermanently, "/carpet setDefault " + rule.name + " " + rule.getAsString());
            m(source, s(rule.toString() + " "), changePermanently);
        } catch (ParsedRule.ValueException e) {
            throw new CommandException(ts("command.carpet.rule.invalidValue", RED, e.message));
        } catch (IllegalArgumentException e) {
            throw new CommandException(ts("command.carpet.rule.invalidValue", RED, e.getMessage()));
        }
        return 1;
    }

    private static <T> int setDefault(ServerCommandSource source, ParsedRule<T> rule, T defaultValue) {
        try {
            rule.set(defaultValue, true);
            rule.save();
            m(source, ts("command.carpet.setDefault.success", "gi", rule.name, defaultValue));
        } catch (ParsedRule.ValueException e) {
            throw new CommandException(ts("command.carpet.rule.invalidValue", RED, e.message));
        }
        return 1;
    }

    private static int removeDefault(ServerCommandSource source, ParsedRule rule) {
        rule.resetToDefault(true);
        rule.save();
        m(source, ts("command.carpet.removeDefault.success", "gi", rule.name, rule.getAsString()));
        return 1;
    }

    private static Text displayInteractiveSetting(ParsedRule<?> rule) {
        Text text = s("- " + rule.name);
        runCommand(text, "/carpet " + rule.name, style(rule.description.deepCopy(), YELLOW));
        boolean first = true;
        for (String option : rule.options) {
            if (first) {
                text.append(" ");
                first = false;
            }
            text.append(makeSetRuleButton(rule, option, true));
        }
        return text;
    }

    private static int listSettings(ServerCommandSource source, Text title, Iterable<ParsedRule<?>> rules) {
        try {
            PlayerEntity player = source.getPlayer();
            title.getStyle().setBold(true);
            m(player, title);
            for (ParsedRule rule : rules) {
                m(player,displayInteractiveSetting(rule));
            }
        } catch (CommandSyntaxException e) {
            m(source, title);
            for (ParsedRule rule : rules) {
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
        m(source, ts("command.carpet.version", DARK_GREEN, Build.NAME, version));
        if (!Build.MINECRAFT_VERSION.equals(SharedConstants.getGameVersion().getId())) {
            m(source, ts("command.carpet.version.builtFor", YELLOW, Build.MINECRAFT_VERSION));
        }
        if (!Build.WORKING_DIR_CLEAN) {
            m(source, ts("command.carpet.version.uncommitted", RED));
        }
        m(source, s(""));
        Collection<QuickCarpetModule> modules = QuickCarpet.getInstance().modules;
        if (!modules.isEmpty()) {
            m(source, ts("command.carpet.modules", BOLD));
            for (QuickCarpetModule m : modules) {
                Text button = s("[" + m.getId() + "]", CYAN);
                runCommand(button, "/carpet list " + m.getId(), ts("command.carpet.modules.list", GRAY));
                m(source, button, s(" " + m.getName() + " " + m.getVersion()));
            }
        }
        m(source, s(""));
        try {
            PlayerEntity player = source.getPlayer();
            Text categories = ts("command.carpet.browseCategories", BOLD);
            boolean first = true;
            for (String name : RULE_CATEGORIES) {
                if (first)  first = false;
                else categories.append(" ");
                categories.append(runCommand(s("[" + name + "]", CYAN), "/carpet list " + name, ts("command.carpet.list", GRAY, name)));
            }
            m(player, categories);
        } catch (CommandSyntaxException ignored) {}
        return 1;
    }
}
