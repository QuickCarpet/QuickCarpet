package quickcarpet.api.settings;

public class TestSettings {
    public static CoreSettingsManager MANAGER = new quickcarpet.settings.impl.CoreSettingsManager();

    @Rule(category = RuleCategory.EXPERIMENTAL)
    public static boolean testBoolean = true;

    @Rule(category = RuleCategory.EXPERIMENTAL, options = {"-1", "0"})
    public static int testInt = -1;

    @Rule(category = RuleCategory.EXPERIMENTAL, validator = Validator.OpLevel.class)
    public static int testCommand = 2;

    public enum TestEnum {
        A, B, C
    }

    @Rule(category = RuleCategory.EXPERIMENTAL)
    public static TestEnum testEnum = TestEnum.A;
}
