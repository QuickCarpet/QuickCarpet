package quickcarpet.settings;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import quickcarpet.module.QuickCarpetModule;
import quickcarpet.network.channels.RulesChannel;
import quickcarpet.utils.Reflection;
import quickcarpet.utils.Translations;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.reflect.Modifier.*;

public final class ParsedRule<T> implements Comparable<ParsedRule> {
    public final Rule rule;
    public final Field field;

    public final String shortName;
    public final String name;
    public final TranslatableText description;
    @Nullable
    public final TranslatableText extraInfo;
    public final ImmutableList<RuleCategory> categories;
    public final ImmutableList<String> options;
    public final Class<T> type;
    private final TypeAdapter<T> typeAdapter;
    public final Validator<T> validator;
    public final ChangeListener<T> onChange;
    public final T defaultValue;
    public final String defaultAsString;
    final SettingsManager manager;
    private T saved;
    private String savedAsString;

    ParsedRule(SettingsManager manager, Field field, Rule rule) {
        if (((field.getModifiers() & (PUBLIC | STATIC | FINAL)) != (PUBLIC | STATIC))) {
            throw new IllegalArgumentException(field + " is not public static");
        }
        this.manager = manager;
        this.rule = rule;
        this.field = field;
        this.shortName = SettingsManager.getDefaultRuleName(field, rule);
        this.name = manager.getRuleName(field, rule);
        this.type = (Class<T>) field.getType();
        this.description = new TranslatableText(manager.getDescriptionTranslationKey(field, rule));
        String extraKey = manager.getExtraTranslationKey(field, rule);
        this.extraInfo = Translations.hasTranslation(extraKey) ? new TranslatableText(extraKey) : null;
        this.categories = ImmutableList.copyOf(rule.category());
        this.validator = Reflection.callPrivateConstructor(rule.validator());
        this.onChange = Reflection.callPrivateConstructor(rule.onChange());
        this.typeAdapter = getTypeAdapter(this.type);
        this.defaultValue = get();
        this.defaultAsString = typeAdapter.toString(this.defaultValue);
        ImmutableList<String> options = typeAdapter.getOptions();
        if (options == null) options = ImmutableList.copyOf(rule.options());
        this.options = options;
    }

    @SuppressWarnings("unchecked")
    private static <T> TypeAdapter<T> getTypeAdapter(Class<T> type) {
        if (type == String.class) return (TypeAdapter<T>) TypeAdapter.STRING;
        if (type == boolean.class) return (TypeAdapter<T>) TypeAdapter.BOOLEAN;
        if (type == int.class) return (TypeAdapter<T>) TypeAdapter.INTEGER;
        if (type == double.class) return (TypeAdapter<T>) TypeAdapter.DOUBLE;
        if (type.isEnum()) return new TypeAdapter.EnumTypeAdapter(type);
        throw new IllegalStateException("Unknown type " + type.getSimpleName());
    }

    public ArgumentType<T> getArgumentType() {
        return typeAdapter.getArgumentType();
    }

    public T getArgument(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return typeAdapter.getArgument(context, "value", type);
    }

    @Nullable
    public QuickCarpetModule getModule() {
        if (!(manager instanceof ModuleSettingsManager)) return null;
        return ((ModuleSettingsManager) manager).module;
    }

    public void set(String value, boolean sync) {
        set(typeAdapter.parse(value), sync);
    }

    public void set(T value, boolean sync) {
        T previousValue = this.get();
        Optional<TranslatableText> error = this.validator.validate(value);
        if (error.isPresent()) throw new ValueException(error.get());
        try {
            this.field.set(null, value);
            this.onChange.onChange(this, previousValue);
            this.categories.forEach(c -> c.onChange(this, previousValue));
            if (sync) RulesChannel.instance.sendRuleUpdate(Collections.singleton(this));
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
        return typeAdapter.toString(get());
    }

    public boolean getBoolValue() {
        if (type == boolean.class) return (Boolean) get();
        if (type.isAssignableFrom(Number.class)) return ((Number) get()).doubleValue() > 0;
        return false;
    }

    public boolean isDefault() {
        return defaultValue.equals(get());
    }

    public void resetToDefault(boolean sync) {
        set(defaultValue, sync);
    }

    public void save() {
        rememberSaved();
        Settings.MANAGER.save();
    }

    void load(String value) {
        set(value, false);
        rememberSaved();
    }

    private void rememberSaved() {
        this.saved = get();
        this.savedAsString = typeAdapter.toString(this.saved);
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

    public interface TypeAdapter<T> {
        T parse(String s);
        ArgumentType<T> getArgumentType();

        default T getArgument(CommandContext<ServerCommandSource> context, String argument, Class<T> type) throws CommandSyntaxException {
            return context.getArgument(argument, type);
        }

        default ImmutableList<String> getOptions() {
            return null;
        }

        default String toString(T value) {
            return String.valueOf(value);
        }

        class Simple<T> implements TypeAdapter<T> {
            private final Function<String, T> parser;
            private final Supplier<ArgumentType<T>> argumentType;

            public Simple(Function<String, T> parser, Supplier<ArgumentType<T>> argumentType) {
                this.parser = parser;
                this.argumentType = argumentType;
            }

            @Override
            public T parse(String s) {
                return parser.apply(s);
            }

            @Override
            public ArgumentType<T> getArgumentType() {
                return argumentType.get();
            }
        }

        class EnumTypeAdapter<T extends Enum<T>> implements TypeAdapter<Enum<T>> {
            public final Class<T> enumClass;

            public EnumTypeAdapter(Class<T> enumClass) {
                this.enumClass = enumClass;
            }

            @Override
            public Enum<T> parse(String s) {
                return Enum.valueOf(enumClass, s.toUpperCase(Locale.ROOT));
            }

            @Override
            public String toString(Enum<T> value) {
                return value.name().toLowerCase(Locale.ROOT);
            }

            @Override
            @SuppressWarnings("unchecked")
            public ArgumentType<Enum<T>> getArgumentType() {
                return (ArgumentType) StringArgumentType.string();
            }

            @Override
            public Enum<T> getArgument(CommandContext<ServerCommandSource> context, String argument, Class<Enum<T>> type) throws CommandSyntaxException {
                try {
                    return parse(StringArgumentType.getString(context, "value"));
                } catch (IllegalArgumentException e) {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().create(String.join(", ", getOptions()));
                }
            }

            @Override
            public ImmutableList<String> getOptions() {
                return Arrays.stream(enumClass.getEnumConstants()).map(e -> e.name().toLowerCase(Locale.ROOT)).collect(ImmutableList.toImmutableList());
            }
        }

        TypeAdapter<String> STRING = new Simple<>(s -> s, StringArgumentType::greedyString);
        TypeAdapter<Boolean> BOOLEAN = new Simple<Boolean>(Boolean::parseBoolean, BoolArgumentType::bool) {
            @Override
            public ImmutableList<String> getOptions() {
                return ImmutableList.of("true", "false");
            }
        };
        TypeAdapter<Integer> INTEGER = new Simple<>(Integer::parseInt, IntegerArgumentType::integer);
        TypeAdapter<Double> DOUBLE = new Simple<>(Double::parseDouble, DoubleArgumentType::doubleArg);
    }

    public static class ValueException extends IllegalArgumentException {
        public final TranslatableText message;

        ValueException(TranslatableText message) {
            this.message = message;
        }

        @Override
        public String getMessage() {
            return message.asFormattedString();
        }
    }
}
