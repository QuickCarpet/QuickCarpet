package quickcarpet.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TextComponent;
import quickcarpet.Build;
import quickcarpet.settings.ParsedRule;
import quickcarpet.settings.RuleCategory;
import quickcarpet.settings.Settings;
import quickcarpet.utils.Messenger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CarpetCommand {
    private static ImmutableList<String> RULE_CATEGORIES = Arrays.stream(RuleCategory.values())
            .map(c -> c.lowerCase)
            .collect(ImmutableList.toImmutableList());

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        LiteralArgumentBuilder<ServerCommandSource> carpet = literal("carpet").requires((player) ->
                player.hasPermissionLevel(2));

        carpet.executes((context)->listAllSettings(context.getSource())).
                then(literal("list").
                        executes( (c) -> listSettings(c.getSource(),
                                "All " + Build.NAME + " Settings",
                                Settings.MANAGER.getRules())).
                        then(literal("defaults").
                                executes( (c)-> listSettings(c.getSource(),
                                        "Current " + Build.NAME + " Startup Settings from carpet.conf",
                                        Settings.MANAGER.getSavedRules()))).
                        then(argument("search", StringArgumentType.word()).
                                suggests( (c, b)-> CommandSource.suggestMatching(RULE_CATEGORIES, b)).
                                executes( (c) -> listSettings(c.getSource(),
                                        String.format("" + Build.NAME + " Settings matching \"%s\"", StringArgumentType.getString(c, "search")),
                                        Settings.MANAGER.getRulesMatching(StringArgumentType.getString(c, "search"))))));

        LiteralArgumentBuilder<ServerCommandSource> removeDefault = literal("removeDefault").requires(s -> !Settings.MANAGER.locked);
        LiteralArgumentBuilder<ServerCommandSource> setDefault = literal("setDefault").requires(s -> !Settings.MANAGER.locked);
        for (ParsedRule rule : Settings.MANAGER.getRules()) {
            carpet.then(literal(rule.name)
                .executes(c -> displayRuleMenu(c.getSource(), rule))
                .then(argument("value", rule.getArgumentType())
                    .suggests((c, b) -> CommandSource.suggestMatching(rule.options, b))
                    .requires(s -> !Settings.MANAGER.locked)
                    .executes(c -> setRule((ServerCommandSource) c.getSource(), rule, c.getArgument("value", rule.type)))
                )
            );
            removeDefault.then(literal(rule.name)
                    .requires(s -> rule.hasSavedValue())
                    .executes(c -> removeDefault(c.getSource(), rule)));
            setDefault.then(literal(rule.name).then(argument("value", rule.getArgumentType())
                .suggests((c, b) -> CommandSource.suggestMatching(rule.options, b))
                .executes(c -> setDefault((ServerCommandSource) c.getSource(), rule, c.getArgument("value", rule.type)))
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
            Messenger.m(source, "w " + rule.name + " is set to: ","wb " + rule.getAsString());
            return 1;
        }

        Messenger.m(player, "");
        Messenger.m(player, "wb " + rule.name, "!/carpet " + rule.name, "^g refresh");
        Messenger.m(player, "w "+rule.description);

        for (String info : rule.extraInfo) {
            Messenger.m(player, "g  " + info);
        }

        List<TextComponent> categories = new ArrayList<>();
        categories.add(Messenger.c("w Categories: "));
        for (String name : RULE_CATEGORIES) {
            categories.add(Messenger.c("c ["+ name +"]", "^g list all "+ name +" settings","!/carpet list " + name));
            categories.add(Messenger.c("w , "));
        }
        categories.remove(categories.size()-1);
        Messenger.m(player, categories.toArray(new Object[0]));

        Messenger.m(player, "w Current value: ",String.format("%s %s (%s value)",rule.getBoolValue()?"lb":"nb", rule.getAsString(),rule.isDefault()?"default":"modified"));
        List<TextComponent> options = new ArrayList<>();
        options.add(Messenger.c("w Options: ", "y [ "));
        for (String o: rule.options) {
            options.add(makeSetRuleButton(rule, o, false));
            options.add(Messenger.c("w  "));
        }
        options.remove(options.size()-1);
        options.add(Messenger.c("y  ]"));
        Messenger.m(player, options.toArray(new Object[0]));
        return 1;
    }

    private static TextComponent makeSetRuleButton(ParsedRule<?> rule, String option, boolean brackets) {
        String color = "";
        if (option.equals(rule.defaultAsString) && !brackets) color += "u";
        color += option.equals(rule.getAsString()) ? "bl" : "y";
        String baseText = color + (brackets ? " [" : " ") + option + (brackets ? "]" : "");
        if (Settings.MANAGER.locked) {
            return Messenger.c(baseText, "^g Settings are locked");
        }
        return Messenger.c(baseText, "^g Switch to " + option, "?/carpet " + rule.name + " " + option);
    }

    private static <T> int setRule(ServerCommandSource source, ParsedRule<T> rule, T newValue) {
        try {
            rule.set(newValue);
            Messenger.m(source, "w "+ rule.toString() + ", ", "c [change permanently?]",
                    "^w Click to keep the settings in carpet.conf to save across restarts",
                    "?/carpet setDefault " + rule.name + " " + rule.getAsString());
        } catch (IllegalArgumentException e) {
            throw new CommandException(Messenger.c("r Invalid value: " + e.getMessage()));
        }
        return 1;
    }

    private static <T> int setDefault(ServerCommandSource source, ParsedRule<T> rule, T defaultValue) {
        try {
            rule.set(defaultValue);
            rule.save();
            Messenger.m(source ,"gi rule " + rule.name + " will now default to "+ defaultValue);
        } catch (IllegalArgumentException e) {
            throw new CommandException(Messenger.c("r Invalid value: " + e.getMessage()));
        }
        return 1;
    }

    private static int removeDefault(ServerCommandSource source, ParsedRule rule) {
        rule.resetToDefault();
        rule.save();
        Messenger.m(source ,"gi rule " + rule.name + " defaults to " + rule.getAsString());
        return 1;
    }

    private static TextComponent displayInteractiveSetting(ParsedRule<?> rule) {
        List<Object> args = new ArrayList<>();
        args.add("w - " + rule.name + " ");
        args.add("!/carpet " + rule.name);
        args.add("^y " + rule.description);
        for (String option : rule.options) {
            args.add(makeSetRuleButton(rule, option, true));
            args.add("w  ");
        }
        args.remove(args.size()-1);
        return Messenger.c(args.toArray(new Object[0]));
    }

    private static int listSettings(ServerCommandSource source, String title, Iterable<ParsedRule<?>> rules) {
        try {
            PlayerEntity player = source.getPlayer();
            Messenger.m(player,String.format("wb %s:",title));
            for (ParsedRule rule : rules) {
                Messenger.m(player,displayInteractiveSetting(rule));
            }
        } catch (CommandSyntaxException e) {
            Messenger.m(source, "w s:"+title);
            for (ParsedRule rule : rules) {
                Messenger.m(source, "w  - "+ rule.toString());
            }
        }
        return 1;
    }

    private static int listAllSettings(ServerCommandSource source) {
        listSettings(source, "Current " + Build.NAME + " Settings", Settings.MANAGER.getNonDefault());

        Messenger.m(source, "e " + Build.NAME + " version: " + Build.VERSION);
        try {
            PlayerEntity player = source.getPlayer();
            List<String> categories = new ArrayList<>();
            categories.add("w Browse Categories:\n");
            for (String name : RULE_CATEGORIES) {
                categories.add("c [" + name + "]");
                categories.add("^g list all " + name + " nsettings");
                categories.add("!/carpet list " + name);
                categories.add("w  ");
            }
            categories.remove(categories.size() - 1);
            Messenger.m(player, categories.toArray(new Object[0]));
        } catch (CommandSyntaxException ignored) {}
        return 1;
    }
}
