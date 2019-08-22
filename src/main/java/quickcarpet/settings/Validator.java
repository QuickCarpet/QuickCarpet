package quickcarpet.settings;

import net.minecraft.text.TranslatableText;
import quickcarpet.utils.Messenger;

import java.util.Optional;

public interface Validator<T> {
    /**
     * Validate the new value of a rule
     * @param value The new value
     * @return empty if valid, error message if invalid
     */
    Optional<TranslatableText> validate(T value);


    class AlwaysTrue<T> implements Validator<T> {
        @Override
        public Optional<TranslatableText> validate(T value) {
            return Optional.empty();
        }
    }

    class Positive<T extends Number> implements Validator<T> {
        @Override
        public Optional<TranslatableText> validate(T value) {
            if(value.doubleValue() > 0) return Optional.empty();
            return Optional.of(Messenger.t("carpet.validator.positive"));
        }
    }

    class NonNegative<T extends Number> implements Validator<T> {
        @Override
        public Optional<TranslatableText> validate(T value) {
            if(value.doubleValue() >= 0) return Optional.empty();
            return Optional.of(Messenger.t("carpet.validator.nonNegative"));
        }
    }

    class Negative<T extends Number> implements Validator<T> {
        @Override
        public Optional<TranslatableText> validate(T value) {
            if(value.doubleValue() < 0) return Optional.empty();
            return Optional.of(Messenger.t("carpet.validator.negative"));
        }
    }
}
