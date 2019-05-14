package quickcarpet.settings;

import com.google.common.collect.ImmutableList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import quickcarpet.Build;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

public class SettingsManager {
    private static final Logger LOG = LogManager.getLogger();
    private final Class<?> settingsClass;
    MinecraftServer server;
    private boolean parsed;
    private boolean initialized;
    private Map<String, ParsedRule<?>> rules = new HashMap<>();
    public boolean locked;

    SettingsManager(Class<?> settingsClass) {
        this.settingsClass = settingsClass;
    }

    void parse() {
        if (parsed) throw new IllegalStateException("Already parsed");
        for (Field f : this.settingsClass.getDeclaredFields()) {
            Rule rule = f.getAnnotation(Rule.class);
            if (rule == null) continue;
            ParsedRule parsed = new ParsedRule(this, f, rule);
            rules.put(parsed.name, parsed);
        }
        this.parsed = true;
    }

    public void init(MinecraftServer server) {
        this.server = server;
        this.initialized = true;
        load();
    }

    public ParsedRule getRule(String name) {
        if (!parsed) throw new IllegalStateException("Not initialized");
        if (!rules.containsKey(name)) throw new IllegalArgumentException("Unknown rule '" + name + "'");
        return rules.get(name);
    }

    public Collection<ParsedRule<?>> getRules() {
        if (!parsed) throw new IllegalStateException("Not initialized");
        return rules.values();
    }

    public Collection<ParsedRule<?>> getNonDefault() {
        return rules.values().stream().filter(r -> !r.isDefault()).collect(ImmutableList.toImmutableList());
    }

    private File getFile() {
        if (!initialized) throw new IllegalStateException("Not initialized");
        return server.getLevelStorage().resolveFile(server.getLevelName(), "carpet.conf");
    }

    public void disableAll(RuleCategory category) {
        for (ParsedRule<?> rule : rules.values()) {
            if (rule.type != boolean.class || !rule.categories.contains(category)) continue;
            ((ParsedRule<Boolean>) rule).set(false);
        }
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

    void resendCommandTree() {
        if (server == null) return;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            server.getCommandManager().sendCommandTree(player);
        }
    }

    void load() {
        for (ParsedRule<?> rule : rules.values()) rule.resetToDefault();
        try (BufferedReader reader = new BufferedReader(new FileReader(getFile()))) {
            for (String line; (line = reader.readLine()) != null;) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.equalsIgnoreCase("locked")) {
                    this.locked = true;
                    LOG.info("[CM]: " + Build.NAME + " is locked by the administrator");
                    disableAll(RuleCategory.COMMANDS);
                    continue;
                }
                String[] kv = line.split("\\s+", 2);
                if (kv.length == 2) {
                    ParsedRule rule = rules.get(kv[0]);
                    if (rule == null) {
                        LOG.error("[CM]: Setting " + kv[0] + " is not a valid - ignoring...");
                        continue;
                    }
                    try {
                        rule.load(kv[1]);
                        LOG.info("[CM]: loaded setting " + kv[0] + " as " + rule.getAsString() + " from carpet.conf");
                    } catch (IllegalArgumentException e) {
                        LOG.error("[CM]: The value " + kv[1] + " for " + kv[0] + " is not valid - ignoring...");
                    }
                } else {
                    LOG.error("[CM]: Unknown line '" + line + "' - ignoring...");
                }
            }
        } catch (FileNotFoundException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Collection<ParsedRule<?>> getRulesMatching(Predicate<ParsedRule<?>> predicate) {
        return rules.values().stream().filter(predicate).collect(ImmutableList.toImmutableList());
    }

    public Collection<ParsedRule<?>> getRulesMatching(String search) {
        String lcSearch = search.toLowerCase(Locale.ROOT);
        return getRulesMatching(rule -> {
            if (rule.name.toLowerCase(Locale.ROOT).contains(lcSearch)) return true;
            for (RuleCategory c : rule.categories) if (c.lowerCase.equals(search)) return true;
            return false;
        });
    }

    public Collection<ParsedRule<?>> getSavedRules() {
        return getRulesMatching(rule -> rule.hasSavedValue());
    }
}
