package quickcarpet.settings;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Pair;
import quickcarpet.Build;
import quickcarpet.QuickCarpet;
import quickcarpet.annotation.BugFix;
import quickcarpet.module.QuickCarpetModule;
import quickcarpet.utils.Reflection;
import quickcarpet.utils.Translations;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class CoreSettingsManager extends SettingsManager {
    private final Map<QuickCarpetModule, ModuleSettingsManager> moduleSettings = new HashMap<>();
    private final List<ParsedRule<?>> allRules = new ArrayList<>();
    private final Map<String, ParsedRule<?>> rulesByName = new HashMap<>();
    private final List<RuleUpgrader> allRuleUpgraders = new ArrayList<>();
    private final Map<QuickCarpetModule, RuleUpgrader> moduleRuleUpgraders = new HashMap<>();
    @Nullable
    private RuleUpgrader coreRuleUpgrader;
    public boolean locked;

    protected CoreSettingsManager(Class<?> settingsClass, @Nullable RuleUpgrader upgrader) {
        super(settingsClass);
        if (upgrader != null) {
            this.coreRuleUpgrader = upgrader;
            this.allRuleUpgraders.add(upgrader);
        }
    }

    @Nullable
    public ModuleSettingsManager getModuleSettings(QuickCarpetModule module) {
        return moduleSettings.get(module);
    }

    @Override
    public void parse() {
        super.parse();
        rulesByName.putAll(this.rules);
        allRules.addAll(this.rules.values());
        for (QuickCarpetModule m : QuickCarpet.getInstance().modules) {
            try {
                Class<?> sc = m.getSettingsClass();
                ModuleSettingsManager manager = new ModuleSettingsManager(m, sc);
                LOG.info("Parsing settings for module " + m.getId() + " from " + sc);
                manager.parse();
                moduleSettings.put(m, manager);
                rulesByName.putAll(manager.rules);
                Collection<ParsedRule<?>> moduleRules = manager.getRules();
                allRules.addAll(moduleRules);
                for (ParsedRule<?> rule : moduleRules) {
                    if (!rulesByName.containsKey(rule.shortName)) {
                        rulesByName.put(rule.shortName, rule);
                    }
                }
                RuleUpgrader upgrader = m.getRuleUpgrader();
                moduleRuleUpgraders.put(m, upgrader);
                if (upgrader != null) allRuleUpgraders.add(upgrader);
            } catch (Exception e) {
                LOG.error("Could not initialize settings for module " + m.getId() + " " + m.getVersion(), e);
            }
        }
        Collections.sort(allRules);
    }

    public void init(MinecraftServer server) {
        super.init(server);
        load();
    }

    @Override
    protected String getTranslationKey(Field field, Rule rule, String key) {
        return "carpet.rule." + getDefaultRuleName(field, rule) + "." + key;
    }

    private Path getFile() {
        if (!initialized) throw new IllegalStateException("Not initialized");
        return QuickCarpet.getConfigFile(Reflection.newWorldSavePath("carpet.conf"));
    }

    @Override
    public Collection<ParsedRule<?>> getRules() {
        return allRules;
    }

    void load() {
        for (ParsedRule<?> rule : allRules) rule.resetToDefault(false);
        try (BufferedReader reader = Files.newBufferedReader(getFile())) {
            for (String line; (line = reader.readLine()) != null;) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.equalsIgnoreCase("locked")) {
                    this.locked = true;
                    LOG.info("[" + Build.NAME + "]: " + Build.NAME + " is locked by the administrator");
                    disableAll(RuleCategory.COMMANDS, false);
                    continue;
                }
                String[] kv = line.split("\\s+", 2);
                if (kv.length == 2) {
                    Pair<String, String> pair = new Pair<>(kv[0], kv[1]);
                    ParsedRule<?> rule = rulesByName.get(kv[0]);
                    if (rule == null) {
                        for (RuleUpgrader upgrader : allRuleUpgraders) {
                            Pair<String, String> upgraded = upgrader.upgrade(pair.getLeft(), pair.getRight());
                            if (upgraded != null) {
                                ParsedRule<?> upgradedRule = rulesByName.get(upgraded.getLeft());
                                if (upgradedRule != null) {
                                    rule = upgradedRule;
                                    pair = upgraded;
                                    break;
                                }
                            }
                        }
                    }
                    if (rule == null) {
                        LOG.error("[" + Build.NAME + "]: Setting " + kv[0] + " is not a valid - ignoring...");
                        continue;
                    }
                    try {
                        String value = pair.getRight();
                        QuickCarpetModule module = rule.getModule();
                        RuleUpgrader upgrader = module == null ? coreRuleUpgrader : moduleRuleUpgraders.get(module);
                        if (upgrader != null) value = upgrader.upgradeValue(rule, value);
                        if (!value.equals(kv[1]) || !pair.getLeft().equals(kv[0])) {
                            LOG.info("[" + Build.NAME + "]: converted "   + kv[0] + "=" + kv[1] + " to " + rule.name + "=" + value);
                        }
                        rule.load(value);
                        LOG.info("[" + Build.NAME + "]: loaded setting " + rule.name + "=" + rule.getAsString() + " from carpet.conf");
                    } catch (IllegalArgumentException e) {
                        LOG.error("[" + Build.NAME + "]: The value " + kv[1] + " for " + rule.name + " is not valid - ignoring...");
                    }
                } else {
                    LOG.error("[" + Build.NAME + "]: Unknown line '" + line + "' - ignoring...");
                }
            }
        } catch (NoSuchFileException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void save() {
        if (locked) return;
        try (PrintStream out = new PrintStream(Files.newOutputStream(getFile()))) {
            for (ParsedRule<?> rule : getNonDefault()) {
                if (!rule.hasSavedValue()) continue;
                out.println(rule.name + " " + rule.getSavedAsString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        resendCommandTree();
    }

    public void dump(OutputStream out) {
        PrintStream ps = new PrintStream(out);
        ps.println("# " + Build.NAME + " Rules");
        for (Map.Entry<String, ParsedRule<?>> e : new TreeMap<>(rules).entrySet()) {
            ParsedRule<?> rule = e.getValue();
            ps.println("## " + rule.name);
            ps.println(Translations.translate(rule.description, Translations.DEFAULT_LOCALE).getString() + "\n");
            if (rule.extraInfo != null) {
                for (String extra : Translations.translate(rule.extraInfo, Translations.DEFAULT_LOCALE).getString().split("\n")) {
                    ps.println(extra + "  ");
                }
                ps.println();
            }
            if (rule.deprecated != null) {
                ps.println("Deprecated: " + Translations.translate(rule.deprecated, Translations.DEFAULT_LOCALE).getString() + "  ");
            }
            ps.println("Type: `" + rule.type.getSimpleName() + "`  ");
            ps.println("Default: `" + rule.defaultAsString + "`  ");
            if (!rule.options.isEmpty()) {
                ps.println("Options: " + rule.options.stream().map(s -> "`" + s + "`").collect(Collectors.joining(", ")) + "  ");
            }
            String categories = rule.categories.stream().map(c -> c.lowerCase).collect(Collectors.joining(", "));
            if (!categories.isEmpty()) ps.println("Categories: " + categories + "  ");
            if (rule.validator.getClass() != Validator.AlwaysTrue.class) ps.println("Validator: `" + rule.validator.getName() + "`  ");
            BugFix[] fixes = rule.rule.bug();
            if (fixes.length > 0) {
                ps.println("Fixes: " + Arrays.stream(fixes).map(fix -> {
                    String s = "[" + fix.value() + "](https://bugs.mojang.com/browse/" + fix.value() + ")";
                    if (!fix.fixVersion().isEmpty()) s += " fixed in " + fix.fixVersion();
                    return s;
                }).collect(Collectors.joining(", ", "", "  ")));
            }
            ps.println();
        }
    }
}
