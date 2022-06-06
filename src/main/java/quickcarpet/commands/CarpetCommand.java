package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import quickcarpet.Build;
import quickcarpet.QuickCarpet;
import quickcarpet.api.module.QuickCarpetModule;
import quickcarpet.api.settings.ParsedRule;
import quickcarpet.api.settings.RuleCategory;
import quickcarpet.settings.Settings;
import quickcarpet.utils.Constants.CarpetCommand.Keys;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static quickcarpet.utils.Constants.CarpetCommand.Texts.*;
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
        var carpet = literal("carpet").requires(p -> p.hasPermissionLevel(2));

        carpet.executes(c -> listAllSettings(c.getSource()))
            .then(literal("list")
                .executes(CarpetCommand::listAllSettings)
                .then(literal("defaults").executes(CarpetCommand::listDefaults))
                .then(argument("search", StringArgumentType.word())
                    .suggests((c, b)-> CommandSource.suggestMatching(RULE_CATEGORIES, b))
                    .executes(CarpetCommand::listSearchResults)));

        var removeDefault = literal("removeDefault").requires(s -> !Settings.MANAGER.isLocked());
        var setDefault = literal("setDefault").requires(s -> !Settings.MANAGER.isLocked());
        for (ParsedRule<?> rule : Settings.MANAGER.getRules()) {
            carpet.then(literal(rule.getName())
                .executes(c -> displayRuleMenu(c.getSource(), rule))
                .then(argument("value", rule.getArgumentType())
                    .suggests((c, b) -> suggestRuleOptions(rule, b))
                    .requires(s -> !Settings.MANAGER.isLocked() && !rule.isDisabled())
                    .executes(c -> setRule(c, rule))
                )
            );
            removeDefault.then(literal(rule.getName())
                .requires(s -> rule.hasSavedValue())
                .executes(c -> removeDefault(c.getSource(), rule)));
            setDefault.then(literal(rule.getName()).then(argument("value", rule.getArgumentType())
                .suggests((c, b) -> suggestRuleOptions(rule, b))
                .executes(c -> setDefault(c, rule))
            ));
        }
        carpet.then(removeDefault);
        carpet.then(setDefault);
        dispatcher.register(carpet);
    }

    private static CompletableFuture<Suggestions> suggestRuleOptions(ParsedRule<?> rule, SuggestionsBuilder b) {
        return CommandSource.suggestMatching(rule.getOptions(), b);
    }

    private static int listAllSettings(CommandContext<ServerCommandSource> c) {
        return listSettings(c.getSource(), TITLE_ALL,
            Settings.MANAGER.getRules().stream().filter(rule -> !rule.isDisabled()).toList()
        );
    }

    private static int listDefaults(CommandContext<ServerCommandSource> c) {
        return listSettings(c.getSource(), TITLE_STARTUP, Settings.MANAGER.getSavedRules());
    }

    private static int listSearchResults(CommandContext<ServerCommandSource> c) {
        return listSettings(c.getSource(), t(Keys.TITLE_SEARCH, Build.NAME, getString(c, "search")),
            Settings.MANAGER.getRulesMatching(getString(c, "search"))
        );
    }

    private static int displayRuleMenu(ServerCommandSource source, ParsedRule<?> rule) {
        if (!(source.getEntity() instanceof ServerPlayerEntity)) {
            m(source, t(Keys.RULE_VALUE, rule.getName(), s(rule.getAsString(), Formatting.BOLD)));
            return 1;
        }

        m(source, s(""));

        String command = "/carpet " + rule.getName();
        MutableText title = runCommand(s(rule.getName(), Formatting.BOLD), command, REFRESH_TOOLTIP);
        if (rule.isDisabled()) title.append(DISABLED_SUFFIX);
        m(source, title);

        m(source, rule.getDescription());

        Text extraInfo = rule.getExtraInfo();
        if (extraInfo != null) m(source, style(extraInfo.shallowCopy(), Formatting.GRAY));

        Text deprecated = rule.getDeprecated();
        if (deprecated != null) m(source, ts(Keys.RULE_DEPRECATED, Formatting.RED, deprecated));

        m(source, join(CATEGORIES, getAllCategories(rule), CarpetCommand::formatCategory, s(", "), null));

        Text valueText = s(rule.getAsString(), rule.getBoolValue() ? Formatting.GREEN : Formatting.DARK_RED, Formatting.BOLD);
        Text status = rule.isDefault() ? DEFAULT : MODIFIED;
        m(source, t(Keys.RULE_CURRENT_VALUE, valueText, status));
        m(source, join(OPTIONS_PREFIX, rule.getOptions(), o -> makeSetRuleButton(rule, o, false), s(" "), OPTIONS_SUFFIX));
        return 1;
    }

    private static Set<String> getAllCategories(ParsedRule<?> rule) {
        Set<String> categories = new LinkedHashSet<>();
        if (rule.getModule() != null) {
            categories.add(rule.getModule().getId());
        }
        for (var category : rule.getCategories()) categories.add(category.lowerCase);
        return categories;
    }

    @NotNull
    private static MutableText formatCategory(String name) {
        return runCommand(
            s("[" + name + "]", Formatting.AQUA),
            "/carpet list " + name,
            t(Keys.LIST, name)
        );
    }

    private static Text makeSetRuleButton(ParsedRule<?> rule, String option, boolean brackets) {
        MutableText baseText = s(brackets ? "[" + option + "]" :  option);
        if (option.equals(rule.getDefaultAsString()) && !brackets) baseText = baseText.formatted(Formatting.UNDERLINE);
        baseText = option.equals(rule.getAsString()) ? baseText.formatted(Formatting.BOLD, Formatting.GREEN) : baseText.formatted(Formatting.YELLOW);
        if (rule.isDisabled()) return hoverText(baseText, DISABLED);
        if (Settings.MANAGER.isLocked()) return hoverText(baseText, LOCKED);
        return suggestCommand(baseText, "/carpet " + rule.getName() + " " + option, ts(Keys.SWITCH, Formatting.GRAY, option));
    }

    private static <T> int setRule(CommandContext<ServerCommandSource> ctx, ParsedRule<T> rule) throws CommandSyntaxException {
        return setRule(ctx.getSource(), rule, rule.getArgument(ctx));
    }

    private static <T> int setRule(ServerCommandSource source, ParsedRule<T> rule, T newValue) {
        try {
            rule.set(newValue, true);
            String command = "/carpet setDefault " + rule.getName() + " " + rule.getAsString();
            m(source, s(rule + " "), suggestCommand(CHANGE_PERMANENTLY.shallowCopy(), command));
        } catch (IllegalArgumentException e) {
            throw commandException(e);
        }
        return 1;
    }

    private static <T> int setDefault(CommandContext<ServerCommandSource> ctx, ParsedRule<T> rule) throws CommandSyntaxException {
        return setDefault(ctx.getSource(), rule, rule.getArgument(ctx));
    }

    private static <T> int setDefault(ServerCommandSource source, ParsedRule<T> rule, T defaultValue) {
        try {
            rule.set(defaultValue, true);
            rule.save();
            m(source, ts(Keys.SET_DEFAULT_SUCCESS, GRAY_ITALIC, rule.getName(), defaultValue));
        } catch (IllegalArgumentException e) {
            throw commandException(e);
        }
        return 1;
    }

    private static CommandException commandException(IllegalArgumentException e) {
        if (e instanceof ParsedRule.ValueException v) {
            return new CommandException(ts(Keys.RULE_INVALID_VALUE, Formatting.RED, v.message));
        } else {
            return new CommandException(ts(Keys.RULE_INVALID_VALUE, Formatting.RED, e.getMessage()));
        }
    }

    private static int removeDefault(ServerCommandSource source, ParsedRule<?> rule) {
        rule.resetToDefault(true);
        rule.save();
        m(source, ts(Keys.REMOVE_DEFAULT_SUCCESS, GRAY_ITALIC, rule.getName(), rule.getAsString()));
        return 1;
    }

    private static Text displayInteractiveSetting(ParsedRule<?> rule) {
        MutableText text = s("- " + rule.getName());
        runCommand(text, "/carpet " + rule.getName(), style(rule.getDescription().shallowCopy(), Formatting.YELLOW));
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
            m(source, title.shallowCopy());
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
        listSettings(source, TITLE_CURRENT, Settings.MANAGER.getNonDefault());

        m(source, s(""));
        m(source, VERSION);
        if (!Build.MINECRAFT_VERSION.equals(SharedConstants.getGameVersion().getId())) m(source, VERSION_BUILT_FOR);
        if (!Build.WORKING_DIR_CLEAN) m(source, UNCOMMITTED);
        m(source, s(""));
        Collection<QuickCarpetModule> modules = QuickCarpet.getInstance().modules;
        if (!modules.isEmpty()) {
            m(source, MODULES);
            for (QuickCarpetModule m : modules) {
                MutableText button = s("[" + m.getId() + "]", Formatting.AQUA);
                runCommand(button, "/carpet list " + m.getId(), MODULES_LIST);
                m(source, button, s(" " + m.getName() + " " + m.getVersion()));
            }
        }
        m(source, s(""));
        if (source.getEntity() instanceof ServerPlayerEntity) {
            m(source, join(BROWSE_CATEGORIES, RULE_CATEGORIES, name -> runCommand(
                s("[" + name + "]", Formatting.AQUA), "/carpet list " + name,
                ts(Keys.LIST, Formatting.GRAY, name)
            ), s(" "), null));
        }
        return 1;
    }
}
