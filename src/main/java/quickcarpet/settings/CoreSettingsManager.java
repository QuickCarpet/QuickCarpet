package quickcarpet.settings;

import net.minecraft.server.MinecraftServer;
import quickcarpet.Build;
import quickcarpet.QuickCarpet;
import quickcarpet.annotation.BugFix;
import quickcarpet.module.QuickCarpetModule;
import quickcarpet.utils.Translations;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class CoreSettingsManager extends SettingsManager {
    private Map<QuickCarpetModule, ModuleSettingsManager> moduleSettings = new HashMap<>();
    private List<ParsedRule<?>> allRules = new ArrayList<>();
    private Map<String, ParsedRule<?>> rulesByName = new HashMap<>();
    public boolean locked;

    protected CoreSettingsManager(Class<?> settingsClass) {
        super(settingsClass);
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
            Class<?> sc = m.getSettingsClass();
            if (sc == null) continue;
            LOG.info("Parsing settings for module " + m.getId() + " from " + sc);
            try {
                ModuleSettingsManager manager = new ModuleSettingsManager(m, sc);
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

    private File getFile() {
        if (!initialized) throw new IllegalStateException("Not initialized");
        return QuickCarpet.getConfigFile("carpet.conf");
    }

    @Override
    public Collection<ParsedRule<?>> getRules() {
        return allRules;
    }

    void load() {
        for (ParsedRule<?> rule : allRules) rule.resetToDefault(false);
        try (BufferedReader reader = new BufferedReader(new FileReader(getFile()))) {
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
                    ParsedRule rule = rulesByName.get(kv[0]);
                    if (rule == null) {
                        LOG.error("[" + Build.NAME + "]: Setting " + kv[0] + " is not a valid - ignoring...");
                        continue;
                    }
                    try {
                        String value = convertRuleDefaultIfNeeded(rule, kv[1]);
                        if (!value.equals(kv[1])) {
                            LOG.info("[" + Build.NAME + "]: converted " + rule.name + " from " + kv[1] + " to " + value);
                        }
                        rule.load(value);
                        LOG.info("[" + Build.NAME + "]: loaded setting " + rule.name + " as " + rule.getAsString() + " from carpet.conf");
                    } catch (IllegalArgumentException e) {
                        LOG.error("[" + Build.NAME + "]: The value " + kv[1] + " for " + rule.name + " is not valid - ignoring...");
                    }
                } else {
                    LOG.error("[" + Build.NAME + "]: Unknown line '" + line + "' - ignoring...");
                }
            }
        } catch (FileNotFoundException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String convertRuleDefaultIfNeeded(ParsedRule<?> rule, String value) {
        if (rule.categories.contains(RuleCategory.COMMANDS) && rule.type == int.class) {
            if ("true".equals(value)) return "0";
            if ("false".equals(value)) return "4";
        }
        return value;
    }

    void save() {
        if (locked) return;
        try (PrintStream out = new PrintStream(new FileOutputStream(getFile()))) {
            for (ParsedRule<?> rule : getNonDefault()) {
                if (!rule.hasSavedValue()) continue;
                out.println(rule.name + " " + rule.getSavedAsString());
            }
        } catch (FileNotFoundException e) {
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
            ps.println(Translations.translate(rule.description, Translations.DEFAULT_LOCALE).asFormattedString() + "\n");
            if (rule.extraInfo != null) {
                for (String extra : Translations.translate(rule.extraInfo, Translations.DEFAULT_LOCALE).asFormattedString().split("\n")) {
                    ps.println(extra + "  ");
                }
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
