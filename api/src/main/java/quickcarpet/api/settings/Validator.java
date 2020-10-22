package quickcarpet.api.settings;

import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.Optional;

public interface Validator<T> {
    /**
     * Validate the new value of a rule
     * @param value The new value
     * @return empty if valid, error message if invalid
     */
    Optional<TranslatableText> validate(T value);

    default String getName() {
        return this.getClass().getName();
    }

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
            return Optional.of(new TranslatableText("carpet.validator.positive"));
        }

        @Override
        public String getName() {
            return "> 0";
        }
    }

    class NonNegative<T extends Number> implements Validator<T> {
        @Override
        public Optional<TranslatableText> validate(T value) {
            if(value.doubleValue() >= 0) return Optional.empty();
            return Optional.of(new TranslatableText("carpet.validator.nonNegative"));
        }

        @Override
        public String getName() {
            return ">= 0";
        }
    }

    class Negative<T extends Number> implements Validator<T> {
        @Override
        public Optional<TranslatableText> validate(T value) {
            if(value.doubleValue() < 0) return Optional.empty();
            return Optional.of(new TranslatableText("carpet.validator.negative"));
        }

        @Override
        public String getName() {
            return "< 0";
        }
    }

    class OpLevel implements Validator<Integer> {
        @Override
        public Optional<TranslatableText> validate(Integer value) {
            if (value >= 0 && value <= 4) return Optional.empty();
            return Optional.of(new TranslatableText("carpet.validator.range", new LiteralText("0").formatted(Formatting.AQUA), new LiteralText("4").formatted(Formatting.AQUA)));
        }

        @Override
        public String getName() {
            return "OP Level (0-4)";
        }
    }

    abstract class Range<T extends Comparable<T>> implements Validator<T> {
        public final T min;
        public final T max;
        public final boolean minIncluded;
        public final boolean maxIncluded;

        protected Range(T min, T max) {
            this(min, max, true, true);
        }

        protected Range(T min, T max, boolean minIncluded, boolean maxIncluded) {
            this.min = min;
            this.max = max;
            this.minIncluded = minIncluded;
            this.maxIncluded = maxIncluded;
        }

        @Override
        public String getName() {
            return "Range " + (minIncluded ? "[" : "(") + min + "," + max + (maxIncluded ? "]" : ")");
        }

        @Override
        public Optional<TranslatableText> validate(T value) {
            int minCompare = value.compareTo(min);
            int maxCompare = value.compareTo(max);
            if ((0 < minCompare && maxCompare < 0) || (minCompare == 0 && minIncluded) || (maxCompare == 0) && maxIncluded) return Optional.empty();
            return Optional.of(new TranslatableText("carpet.validator.range", new LiteralText(this.min.toString()).formatted(Formatting.AQUA), new LiteralText(this.max.toString()).formatted(Formatting.AQUA)));
        }
    }
}
