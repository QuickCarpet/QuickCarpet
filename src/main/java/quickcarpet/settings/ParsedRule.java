package quickcarpet.settings;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.CommandSource;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class ParsedRule<T> implements Comparable<ParsedRule> {
    public final Rule rule;
    public final Field field;

    public final String name;
    public final String description;
    public final ImmutableList<String> extraInfo;
    public final ImmutableList<RuleCategory> categories;
    public final ImmutableList<String> options;
    public final Class<T> type;
    public final Validator<T> validator;
    public final ChangeListener<T> onChange;
    public final T defaultValue;
    public final String defaultAsString;
    final SettingsManager manager;
    private T saved;
    private String savedAsString;

    ParsedRule(SettingsManager manager, Field field, Rule rule) throws IllegalAccessException, InstantiationException {
        this.manager = manager;
        this.rule = rule;
        this.field = field;
        this.name = rule.name().isEmpty() ? field.getName() : rule.name();
        this.type = (Class<T>) field.getType();
        this.description = rule.desc();
        this.extraInfo = ImmutableList.copyOf(rule.extra());
        this.categories = ImmutableList.copyOf(rule.category());
        this.validator = ((Class<Validator<T>>) rule.validator()).newInstance();
        this.onChange = ((Class<ChangeListener<T>>) rule.onChange()).newInstance();
        this.defaultValue = get();
        this.defaultAsString = convertToString(this.defaultValue);
        if (this.type == boolean.class) {
            this.options = ImmutableList.of("true", "false");
        } else if (this.type.isEnum()) {
            this.options = Arrays.stream(this.type.getEnumConstants()).map(e -> ((Enum) e).name().toLowerCase(Locale.ROOT)).collect(ImmutableList.toImmutableList());
        } else {
            this.options = ImmutableList.copyOf(rule.options());
        }
    }

    public ArgumentType<T> getArgumentType() {
        if (type == String.class) return (ArgumentType<T>) StringArgumentType.greedyString();
        if (type == boolean.class) return (ArgumentType<T>) BoolArgumentType.bool();
        if (type == int.class) return (ArgumentType<T>) IntegerArgumentType.integer();
        if (type.isEnum()) return new ArgumentType<T>() {
            @Override
            public T parse(StringReader reader) throws CommandSyntaxException {
                int start = reader.getCursor();
                String value = reader.readUnquotedString();
                String ucValue = value.toUpperCase(Locale.ROOT);
                try {
                    return (T) Enum.valueOf((Class<? extends Enum>) type, ucValue);
                } catch (IllegalArgumentException e) {
                    reader.setCursor(start);
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().createWithContext(reader, value);
                }
            }

            @Override
            public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
                return CommandSource.suggestMatching(options, builder);
            }
        };
        throw new IllegalStateException("Unknown type " + type.getSimpleName());
    }

    public void set(String value) {
        if (type == String.class) {
            set((T) value);
        } else if (type == boolean.class) {
            set((T) (Object) Boolean.parseBoolean(value));
        } else if (type == int.class) {
            set((T) (Object) Integer.parseInt(value));
        } else if (type.isEnum()) {
            String ucValue = value.toUpperCase(Locale.ROOT);
            set((T) (Object) Enum.valueOf((Class<? extends Enum>) type, ucValue));
        } else throw new IllegalStateException("Unknown type " + type.getSimpleName());
    }

    public void set(T value) {
        Optional<String> error = this.validator.validate(value);
        if (error.isPresent()) throw new IllegalArgumentException(error.get());
        try {
            this.field.set(null, value);
            this.onChange.onChange(this);
            this.categories.forEach(c -> c.onChange(this));
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public T get() {
        try {
            return (T) this.field.get(null);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public String getAsString() {
        return convertToString(get());
    }

    public boolean getBoolValue() {
        if (type == boolean.class) return (Boolean) get();
        if (type.isAssignableFrom(Number.class)) return ((Number) get()).doubleValue() > 0;
        return false;
    }

    public boolean isDefault() {
        return defaultValue.equals(get());
    }

    public void resetToDefault() {
        set(defaultValue);
    }

    public void save() {
        rememberSaved();
        this.manager.save();
    }

    void load(String value) {
        set(value);
        rememberSaved();
    }

    private void rememberSaved() {
        this.saved = get();
        this.savedAsString = convertToString(this.saved);
    }

    T getSaved() {
        return saved;
    }

    String getSavedAsString() {
        return savedAsString;
    }

    public boolean hasSavedValue() {
        if (saved == null) return false;
        return !defaultValue.equals(saved);
    }

    private static String convertToString(Object value) {
        if (value instanceof Enum) return ((Enum) value).name().toLowerCase(Locale.ROOT);
        return value.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == ParsedRule.class && ((ParsedRule) obj).name.equals(this.name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public int compareTo(ParsedRule o) {
        return this.name.compareTo(o.name);
    }

    @Override
    public String toString() {
        return this.name + ": " + getAsString();
    }
}
