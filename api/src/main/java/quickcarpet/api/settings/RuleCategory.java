package quickcarpet.api.settings;

import java.util.Locale;

public enum RuleCategory implements ChangeListener<Object> {
    TNT, FIX, SURVIVAL, CREATIVE, EXPERIMENTAL, OPTIMIZATIONS, FEATURE, RENEWABLE,
    COMMANDS {
        @Override
        public void onChange(ParsedRule<Object> rule, Object previous) {
            rule.getManager().resendCommandTree();
        }
    };

    public final String lowerCase;

    RuleCategory() {
        this.lowerCase = this.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public void onChange(ParsedRule<Object> rule, Object previous) {
    }
}
