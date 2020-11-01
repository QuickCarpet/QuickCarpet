package quickcarpet.api.settings;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class SettingsManagerTest {
    @BeforeAll
    static void parse() {
        TestSettings.MANAGER.parse();
    }

    @Test
    void testBoolean() {
        assertTrue(TestSettings.testBoolean);
        ParsedRule<?> rule = TestSettings.MANAGER.getRule("testBoolean");
        assertEquals(boolean.class, rule.getType());
        assertEquals(Arrays.asList("true", "false"), rule.getOptions());
        rule.set("false", false);
        assertFalse(TestSettings.testBoolean);
    }

    @Test
    void testInt() {
        assertEquals(-1, TestSettings.testInt);
        ParsedRule<?> rule = TestSettings.MANAGER.getRule("testInt");
        assertEquals(int.class, rule.getType());
        assertEquals(Arrays.asList("-1", "0"), rule.getOptions());
        rule.set("0", false);
        assertEquals(0, TestSettings.testInt);
    }

    @Test
    void testCommand() {
        assertEquals(2, TestSettings.testCommand);
        ParsedRule<?> rule = TestSettings.MANAGER.getRule("testCommand");
        assertEquals(int.class, rule.getType());
        assertTrue(rule.getValidator() instanceof Validator.OpLevel);
        rule.set("0", false);
        assertEquals(0, TestSettings.testCommand);
        rule.set("4", false);
        assertEquals(4, TestSettings.testCommand);
        assertThrows(ParsedRule.ValueException.class, () -> rule.set("5", false));
    }

    @Test
    void testEnum() {
        assertEquals(TestSettings.TestEnum.A, TestSettings.testEnum);
        ParsedRule<?> rule = TestSettings.MANAGER.getRule("testEnum");
        assertEquals(TestSettings.TestEnum.class, rule.getType());
        assertEquals(Arrays.asList("a", "b", "c"), rule.getOptions());
        rule.set("b", false);
        assertEquals(TestSettings.TestEnum.B, TestSettings.testEnum);
    }
}
