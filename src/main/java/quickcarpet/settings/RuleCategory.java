package quickcarpet.settings;

import java.util.Locale;

public enum RuleCategory implements ChangeListener {
    TNT, FIX, SURVIVAL, CREATIVE, EXPERIMENTAL, OPTIMIZATIONS, FEATURE,
    COMMANDS {
        @Override
        public void onChange(ParsedRule rule, Object previous) {
            rule.manager.resendCommandTree();
        }
    };

    public final String lowerCase;

    RuleCategory() {
        this.lowerCase = this.name().toLowerCase(Locale.ROOT);
    }

    public void onChange(ParsedRule rule, Object previous) {
    }
}
