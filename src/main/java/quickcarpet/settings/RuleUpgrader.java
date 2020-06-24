package quickcarpet.settings;

import net.minecraft.util.Pair;

public interface RuleUpgrader {
    default Pair<String, String> upgrade(String key, String value) {
        return null;
    }

    default String upgradeValue(ParsedRule<?> rule, String value) {
        return value;
    }
}
