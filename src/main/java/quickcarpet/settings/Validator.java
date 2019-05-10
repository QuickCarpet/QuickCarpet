package quickcarpet.settings;

import java.util.Optional;

interface Validator<T> {
    /**
     * Validate the new value of a rule
     * @param value The new value
     * @return empty if valid, error message if invalid
     */
    Optional<String> validate(T value);


    class AlwaysTrue<T> implements Validator<T> {
        @Override
        public Optional<String> validate(T value) {
            return Optional.empty();
        }
    }

    class Positive<T extends Number> implements Validator<T> {
        @Override
        public Optional<String> validate(T value) {
            if(value.doubleValue() > 0) return Optional.empty();
            return Optional.of("Must be positive");
        }
    }

    class NonNegative<T extends Number> implements Validator<T> {
        @Override
        public Optional<String> validate(T value) {
            if(value.doubleValue() >= 0) return Optional.empty();
            return Optional.of("Must be non-negative");
        }
    }

    class Negative<T extends Number> implements Validator<T> {
        @Override
        public Optional<String> validate(T value) {
            if(value.doubleValue() < 0) return Optional.empty();
            return Optional.of("Must be negative");
        }
    }
}
