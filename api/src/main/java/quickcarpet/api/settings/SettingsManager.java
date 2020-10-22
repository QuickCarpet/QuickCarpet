package quickcarpet.api.settings;

import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Predicate;

public interface SettingsManager {
    ParsedRule<?> getRule(String name);
    Collection<ParsedRule<?>> getRules();
    Collection<ParsedRule<?>> getNonDefault();
    void disableAll(RuleCategory category, boolean sync);
    void resendCommandTree();
    Collection<ParsedRule<?>> getRulesMatching(Predicate<ParsedRule<?>> predicate);
    Collection<ParsedRule<?>> getRulesMatching(String search);
    Collection<ParsedRule<?>> getSavedRules();
    @Nullable
    RuleUpgrader getRuleUpgrader();
    void parse();
    void init(MinecraftServer server);
}
