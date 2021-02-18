package quickcarpet.settings.impl;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import quickcarpet.api.module.QuickCarpetModule;
import quickcarpet.api.settings.*;
import quickcarpet.network.channels.RulesChannel;
import quickcarpet.settings.Settings;
import quickcarpet.utils.Messenger;
import quickcarpet.utils.MixinConfig;
import quickcarpet.utils.Reflection;
import quickcarpet.utils.Translations;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.reflect.Modifier.*;

final class ParsedRuleImpl<T> implements Comparable<ParsedRule<T>>, ParsedRule<T> {
    private final Rule rule;
    private final Field field;

    private final String shortName;
    private final String name;
    private final TranslatableText description;
    @Nullable
    private final TranslatableText extraInfo;
    @Nullable
    private final TranslatableText deprecated;
    private final ImmutableList<RuleCategory> categories;
    private final ImmutableList<String> options;
    private final ImmutableList<String> enabledOptions;
    private final Class<T> type;
    private final TypeAdapter<T, ?> typeAdapter;
    private final Validator<T> validator;
    private final ChangeListener<T> onChange;
    private final T defaultValue;
    private final String defaultAsString;
    private final quickcarpet.api.settings.SettingsManager manager;
    private final boolean disabled;
    private T saved;
    private String savedAsString;

    ParsedRuleImpl(SettingsManager manager, Field field, Rule rule) {
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
        this.validator = Reflection.callPrivateConstructor((Class<Validator<T>>) rule.validator());
        this.onChange = Reflection.callPrivateConstructor((Class<ChangeListener<T>>) rule.onChange());
        this.typeAdapter = getTypeAdapter(this.type);
        this.defaultValue = get();
        this.defaultAsString = typeAdapter.toString(this.defaultValue);
        boolean disabled = !MixinConfig.INSTANCE.isRuleEnabled(this);
        ImmutableList<String> options = typeAdapter.getOptions();
        if (options == null) options = ImmutableList.copyOf(rule.options());
        this.options = options;
        if (!disabled) {
            this.enabledOptions = options.stream()
                .filter(option -> MixinConfig.INSTANCE.isOptionEnabled(this, option))
                .collect(ImmutableList.toImmutableList());
            if (!options.isEmpty() && enabledOptions.size() <= 1) disabled = true;
        } else {
            this.enabledOptions = ImmutableList.of();
        }
        this.disabled = disabled;
        this.deprecated = rule.deprecated() ? new TranslatableText(manager.getDeprecationTranslationKey(field, rule)) : null;
    }

    @Override
    public Rule getRule() {
        return rule;
    }

    @Override
    public Field getField() {
        return field;
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TranslatableText getDescription() {
        return description;
    }

    @Nullable
    @Override
    public TranslatableText getExtraInfo() {
        return extraInfo;
    }

    @Nullable
    @Override
    public TranslatableText getDeprecated() {
        return deprecated;
    }

    @Override
    public List<RuleCategory> getCategories() {
        return categories;
    }

    @Override
    public List<String> getOptions() {
        return enabledOptions;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public Validator<T> getValidator() {
        return validator;
    }

    @Override
    public ChangeListener<T> getChangeListener() {
        return onChange;
    }

    @Override
    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public T getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String getDefaultAsString() {
        return defaultAsString;
    }

    @SuppressWarnings("unchecked")
    private static <T> TypeAdapter<T, ?> getTypeAdapter(Class<T> type) {
        if (type == String.class) return (TypeAdapter<T, ?>) TypeAdapter.STRING;
        if (type == boolean.class) return (TypeAdapter<T, ?>) TypeAdapter.BOOLEAN;
        if (type == int.class) return (TypeAdapter<T, ?>) TypeAdapter.INTEGER;
        if (type == double.class) return (TypeAdapter<T, ?>) TypeAdapter.DOUBLE;
        if (type.isEnum()) return new TypeAdapter.EnumTypeAdapter(type);
        throw new IllegalStateException("Unknown type " + type.getSimpleName());
    }

    @Override
    public ArgumentType<?> getArgumentType() {
        return typeAdapter.getArgumentType();
    }

    @Override
    public T getArgument(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return typeAdapter.getArgument(context, "value", type);
    }

    @Override
    @Nullable
    public QuickCarpetModule getModule() {
        if (!(manager instanceof ModuleSettingsManager)) return null;
        return ((ModuleSettingsManager) manager).module;
    }

    @Override
    public quickcarpet.api.settings.SettingsManager getManager() {
        return manager;
    }

    @Override
    public void set(String value, boolean sync) {
        set(typeAdapter.parse(value), sync);
    }

    @Override
    public void set(T value, boolean sync) {
        T previousValue = this.get();
        Optional<TranslatableText> error = this.validator.validate(value);
        if (error.isPresent()) throw new ParsedRule.ValueException(error.get());
        if (sync) {
            String str = typeAdapter.toString(value);
            if (options.contains(str) && !enabledOptions.contains(str)) {
                throw new ParsedRule.ValueException(Messenger.t(""));
            }
        }
        try {
            this.field.set(null, value);
            this.onChange.onChange(this, previousValue);
            this.categories.forEach(c -> c.onChange((ParsedRuleImpl<Object>) this, previousValue));
            if (sync) RulesChannel.instance.sendRuleUpdate(Collections.singleton(this));
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        try {
            return (T) this.field.get(null);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getAsString() {
        return typeAdapter.toString(get());
    }

    @Override
    public boolean getBoolValue() {
        if (type == boolean.class) return (Boolean) get();
        if (type.isAssignableFrom(Number.class)) return ((Number) get()).doubleValue() > 0;
        return false;
    }

    @Override
    public boolean isDefault() {
        return defaultValue.equals(get());
    }

    @Override
    public void resetToDefault(boolean sync) {
        set(defaultValue, sync);
    }

    @Override
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

    @Override
    public T getSaved() {
        return saved;
    }

    @Override
    public String getSavedAsString() {
        return savedAsString;
    }

    @Override
    public boolean hasSavedValue() {
        if (saved == null) return false;
        return !defaultValue.equals(saved);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ParsedRule && ((ParsedRule<?>) obj).getName().equals(this.name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public int compareTo(ParsedRule<T> o) {
        return this.name.compareTo(o.getName());
    }

    @Override
    public String toString() {
        return this.name + ": " + getAsString();
    }

    public interface TypeAdapter<T, A> {
        T parse(String s);
        ArgumentType<A> getArgumentType();

        default T getArgument(CommandContext<ServerCommandSource> context, String argument, Class<T> type) throws CommandSyntaxException {
            return context.getArgument(argument, type);
        }

        default ImmutableList<String> getOptions() {
            return null;
        }

        default String toString(T value) {
            return String.valueOf(value);
        }

        class Simple<T> implements TypeAdapter<T, T> {
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

        class EnumTypeAdapter<T extends Enum<T>> implements TypeAdapter<Enum<T>, String> {
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
            public ArgumentType<String> getArgumentType() {
                return StringArgumentType.string();
            }

            @Override
            public Enum<T> getArgument(CommandContext<ServerCommandSource> context, String argument, Class<Enum<T>> type) throws CommandSyntaxException {
                try {
                    return parse(StringArgumentType.getString(context, argument));
                } catch (IllegalArgumentException e) {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().create(String.join(", ", getOptions()));
                }
            }

            @Override
            public ImmutableList<String> getOptions() {
                return Arrays.stream(enumClass.getEnumConstants()).map(e -> e.name().toLowerCase(Locale.ROOT)).collect(ImmutableList.toImmutableList());
            }
        }

        TypeAdapter.Simple<String> STRING = new Simple<>(s -> s, StringArgumentType::greedyString);
        TypeAdapter.Simple<Boolean> BOOLEAN = new Simple<Boolean>(Boolean::parseBoolean, BoolArgumentType::bool) {
            @Override
            public ImmutableList<String> getOptions() {
                return ImmutableList.of("true", "false");
            }
        };
        TypeAdapter.Simple<Integer> INTEGER = new Simple<>(Integer::parseInt, IntegerArgumentType::integer);
        TypeAdapter.Simple<Double> DOUBLE = new Simple<>(Double::parseDouble, DoubleArgumentType::doubleArg);
    }

}
