package quickcarpet.settings;

import com.google.common.collect.ImmutableList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import quickcarpet.module.QuickCarpetModule;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

public abstract class SettingsManager {
    protected static final Logger LOG = LogManager.getLogger();
    protected final Class<?> settingsClass;
    MinecraftServer server;
    protected boolean parsed;
    protected boolean initialized;
    protected Map<String, ParsedRule<?>> rules = new HashMap<>();

    protected SettingsManager(Class<?> settingsClass) {
        this.settingsClass = settingsClass;
    }

    public void parse() {
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
    }

    public static String getDefaultRuleName(Field field, Rule rule) {
        return rule.name().isEmpty() ? field.getName() : rule.name();
    }

    public String getRuleName(Field field, Rule rule) {
        return getDefaultRuleName(field, rule);
    }

    public String getDescriptionTranslationKey(Field field, Rule rule) {
        return "carpet.rule." + getDefaultRuleName(field, rule) + ".description";
    }

    public String getExtraTranslationKey(Field field, Rule rule) {
        return "carpet.rule." + getDefaultRuleName(field, rule) + ".extra";
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
        return getRules().stream().filter(r -> !r.isDefault()).collect(ImmutableList.toImmutableList());
    }

    public void disableAll(RuleCategory category, boolean sync) {
        for (ParsedRule<?> rule : getRules()) {
            if (rule.type != boolean.class || !rule.categories.contains(category)) continue;
            ((ParsedRule<Boolean>) rule).set(false, sync);
        }
    }

    void resendCommandTree() {
        if (server == null) return;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            server.getCommandManager().sendCommandTree(player);
        }
    }

    public Collection<ParsedRule<?>> getRulesMatching(Predicate<ParsedRule<?>> predicate) {
        return getRules().stream().filter(predicate).collect(ImmutableList.toImmutableList());
    }

    public Collection<ParsedRule<?>> getRulesMatching(String search) {
        String lcSearch = search.toLowerCase(Locale.ROOT);
        return getRulesMatching(rule -> {
            if (rule.name.toLowerCase(Locale.ROOT).contains(lcSearch)) return true;
            for (RuleCategory c : rule.categories) if (c.lowerCase.equals(search)) return true;
            QuickCarpetModule module = rule.getModule();
            if (module != null) return module.getId().toLowerCase(Locale.ROOT).contains(lcSearch);
            return false;
        });
    }

    public Collection<ParsedRule<?>> getSavedRules() {
        return getRulesMatching(ParsedRule::hasSavedValue);
    }
}
