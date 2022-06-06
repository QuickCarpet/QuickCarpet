package quickcarpet.settings.impl;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import quickcarpet.Build;
import quickcarpet.api.module.QuickCarpetModule;
import quickcarpet.api.settings.*;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;

import static java.lang.reflect.Modifier.*;

abstract class SettingsManager implements quickcarpet.api.settings.SettingsManager, RuleCreator {
    protected static final Logger LOG = LogManager.getLogger(Build.NAME);
    protected final Class<?> settingsClass;
    MinecraftServer server;
    protected boolean parsed;
    protected boolean initialized;
    protected Map<String, ParsedRule<?>> rules = new HashMap<>();
    @Nullable
    protected final RuleUpgrader ruleUpgrader;

    protected SettingsManager(Class<?> settingsClass) {
        this.settingsClass = settingsClass;

        RuleUpgrader upgrader = null;
        try {
            Field ruleUpgraderField = settingsClass.getField("RULE_UPGRADER");
            if (((ruleUpgraderField.getModifiers() & (PUBLIC | STATIC | FINAL)) != (PUBLIC | STATIC | FINAL))) {
                throw new IllegalArgumentException(ruleUpgraderField + " is not public static final");
            }
            if (!RuleUpgrader.class.isAssignableFrom(ruleUpgraderField.getType())){
                throw new IllegalArgumentException(ruleUpgraderField + " is not of type RuleUpgrader");
            }
            upgrader = (RuleUpgrader) ruleUpgraderField.get(null);
            if (upgrader == null) {
                throw new IllegalArgumentException(ruleUpgraderField + " is null");
            }
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        this.ruleUpgrader = upgrader;
    }

    @Override
    public void parse() {
        if (parsed) throw new IllegalStateException("Already parsed");
        for (Field f : this.settingsClass.getDeclaredFields()) {
            Rule rule = f.getAnnotation(Rule.class);
            if (rule == null) continue;
            ParsedRuleImpl<?> parsed = new ParsedRuleImpl<>(this, f, rule);
            rules.put(parsed.getName(), parsed);
        }
        this.parsed = true;
    }

    @Override
    public <T> ParsedRule<T> create(String name, FieldAccessor<T> field, List<RuleCategory> categories, List<String> options, Validator<T> validator, ChangeListener<T> changeListener, boolean deprecated) {
        ParsedRule<T> rule = new ParsedRuleImpl<>(this, name, field, categories, options, validator, changeListener, deprecated);
        rules.put(rule.getName(), rule);
        return rule;
    }

    @Override
    public void init(MinecraftServer server) {
        this.server = server;
        this.initialized = true;
    }

    public static String getDefaultRuleName(String fieldName, Rule rule) {
        return rule.name().isEmpty() ? fieldName : rule.name();
    }

    public String getRuleName(String fieldName, Rule rule) {
        return getDefaultRuleName(fieldName, rule);
    }

    protected abstract String getTranslationKey(String fieldName, Rule rule, String key);

    public String getDescriptionTranslationKey(String fieldName, Rule rule) {
        return getTranslationKey(fieldName, rule, "description");
    }

    public String getExtraTranslationKey(String fieldName, Rule rule) {
        return getTranslationKey(fieldName, rule, "extra");
    }

    public String getDeprecationTranslationKey(String fieldName, Rule rule) {
        return getTranslationKey(fieldName, rule, "deprecated");
    }

    @Override
    public ParsedRule<?> getRule(String name) {
        if (!parsed) throw new IllegalStateException("Not initialized");
        if (!rules.containsKey(name)) throw new IllegalArgumentException("Unknown rule '" + name + "'");
        return rules.get(name);
    }

    @Override
    public Collection<ParsedRule<?>> getRules() {
        if (!parsed) throw new IllegalStateException("Not initialized");
        return rules.values();
    }

    @Override
    public Collection<ParsedRule<?>> getNonDefault() {
        return getRules().stream().filter(r -> !r.isDefault()).toList();
    }

    @Override
    public void disableAll(RuleCategory category, boolean sync) {
        for (ParsedRule<?> rule : getRules()) {
            if (rule.getType() != boolean.class || !rule.getCategories().contains(category)) continue;
            //noinspection unchecked
            ((ParsedRule<Boolean>) rule).set(false, sync);
        }
    }

    @Override
    public void resendCommandTree() {
        if (server == null) return;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            server.getCommandManager().sendCommandTree(player);
        }
    }

    @Override
    public Collection<ParsedRule<?>> getRulesMatching(Predicate<ParsedRule<?>> predicate) {
        return getRules().stream().filter(predicate).toList();
    }

    @Override
    public Collection<ParsedRule<?>> getRulesMatching(String search) {
        String lcSearch = search.toLowerCase(Locale.ROOT);
        return getRulesMatching(rule -> {
            if (rule.getName().toLowerCase(Locale.ROOT).contains(lcSearch)) return true;
            for (RuleCategory c : rule.getCategories()) if (c.lowerCase.equals(search)) return true;
            QuickCarpetModule module = rule.getModule();
            if (module != null) return module.getId().toLowerCase(Locale.ROOT).contains(lcSearch);
            return false;
        });
    }

    @Override
    public Collection<ParsedRule<?>> getSavedRules() {
        return getRulesMatching(ParsedRule::hasSavedValue);
    }

    @Override
    @Nullable
    public RuleUpgrader getRuleUpgrader() {
        return ruleUpgrader;
    }
}
