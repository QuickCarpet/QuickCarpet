package quickcarpet.api.settings;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ValidatorTest {
    @Test
    void alwaysTrue() {
        Validator<?> validator = new Validator.AlwaysTrue<>();
        valid(validator, null);
    }

    @Test
    void positive() {
        Validator<Number> validator = new Validator.Positive<>();
        valid(validator, 1);
        valid(validator, 1.0);
        valid(validator, Double.POSITIVE_INFINITY);
        invalid(validator, 0);
        invalid(validator, 0.0);
        invalid(validator, -0.0);
        invalid(validator, -1);
        invalid(validator, -1.0);
        invalid(validator, Double.NEGATIVE_INFINITY);
        invalid(validator, Double.NaN);
    }

    @Test
    void nonNegative() {
        Validator<Number> validator = new Validator.NonNegative<>();
        valid(validator, 1);
        valid(validator, 1.0);
        valid(validator, Double.POSITIVE_INFINITY);
        valid(validator, 0);
        valid(validator, 0.0);
        valid(validator, -0.0);
        invalid(validator, -1);
        invalid(validator, -1.0);
        invalid(validator, Double.NEGATIVE_INFINITY);
        invalid(validator, Double.NaN);
    }

    @Test
    void negative() {
        Validator<Number> validator = new Validator.Negative<>();
        invalid(validator, 1);
        invalid(validator, 1.0);
        invalid(validator, Double.POSITIVE_INFINITY);
        invalid(validator, 0);
        invalid(validator, 0.0);
        invalid(validator, -0.0);
        valid(validator, -1);
        valid(validator, -1.0);
        valid(validator, Double.NEGATIVE_INFINITY);
        invalid(validator, Double.NaN);
    }

    @Test
    void opLevel() {
        Validator<Integer> validator = new Validator.OpLevel();
        invalid(validator, -1);
        valid(validator, 0);
        valid(validator, 1);
        valid(validator, 2);
        valid(validator, 3);
        valid(validator, 4);
        invalid(validator, 5);
    }

    private static <T> void valid(Validator<T> validator, T value) {
        assertEquals(Optional.empty(), validator.validate(value), "Expected " + value + " to be valid for '" + validator.getName() + "'");
    }
    
    private static <T> void invalid(Validator<T> validator, T value) {
        assertNotEquals(Optional.empty(), validator.validate(value), "Expected " + value + " to be invalid for '" + validator.getName() + "'");
    }
}
