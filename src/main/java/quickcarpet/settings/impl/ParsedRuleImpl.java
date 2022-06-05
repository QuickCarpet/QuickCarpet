package quickcarpet.settings.impl;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import quickcarpet.api.module.QuickCarpetModule;
import quickcarpet.api.settings.*;
import quickcarpet.network.channels.RulesChannel;
import quickcarpet.settings.Settings;
import quickcarpet.utils.MixinConfig;
import quickcarpet.utils.Reflection;
import quickcarpet.utils.Translations;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.reflect.Modifier.*;
import static quickcarpet.utils.Messenger.t;

final class ParsedRuleImpl<T> implements Comparable<ParsedRule<T>>, ParsedRule<T> {
    private final Rule rule;
    private final Field field;
    private final VarHandle varHandle;
    private final boolean fieldVolatile;

    private final String shortName;
    private final String name;
    private final Text description;
    @Nullable
    private final Text extraInfo;
    @Nullable
    private final Text deprecated;
    private final List<RuleCategory> categories;
    private final List<String> options;
    private final List<String> enabledOptions;
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

    @SuppressWarnings("unchecked")
    ParsedRuleImpl(SettingsManager manager, Field field, Rule rule) {
        int fieldAccess = field.getModifiers();
        if (((fieldAccess & (PUBLIC | STATIC | FINAL)) != (PUBLIC | STATIC))) {
            throw new IllegalArgumentException(field + " is not public static");
        }
        this.manager = manager;
        this.rule = rule;
        this.field = field;
        try {
            this.varHandle = MethodHandles.lookup().unreflectVarHandle(field);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
        this.fieldVolatile = (fieldAccess & VOLATILE) != 0;
        this.shortName = SettingsManager.getDefaultRuleName(field, rule);
        this.name = manager.getRuleName(field, rule);
        this.type = (Class<T>) field.getType();
        this.description = t(manager.getDescriptionTranslationKey(field, rule));
        String extraKey = manager.getExtraTranslationKey(field, rule);
        this.extraInfo = Translations.hasTranslation(extraKey) ? t(extraKey) : null;
        this.categories = ImmutableList.copyOf(rule.category());
        this.validator = (Validator<T>) Reflection.callDeprecatedPrivateConstructor(rule.validator());
        this.onChange = (ChangeListener<T>) Reflection.callDeprecatedPrivateConstructor(rule.onChange());
        this.typeAdapter = getTypeAdapter(this.type);
        this.defaultValue = get();
        this.defaultAsString = typeAdapter.toString(this.defaultValue);
        boolean disabled = !MixinConfig.getInstance().isRuleEnabled(this);
        List<String> options = typeAdapter.getOptions();
        if (options == null) options = ImmutableList.copyOf(rule.options());
        this.options = options;
        if (!disabled) {
            this.enabledOptions = options.stream()
                .filter(option -> MixinConfig.getInstance().isOptionEnabled(this, option))
                .toList();
            if (!options.isEmpty() && enabledOptions.size() <= 1) disabled = true;
        } else {
            this.enabledOptions = List.of(defaultAsString);
        }
        this.disabled = disabled;
        this.deprecated = rule.deprecated() ? t(manager.getDeprecationTranslationKey(field, rule)) : null;
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
    public Text getDescription() {
        return description;
    }

    @Nullable
    @Override
    public Text getExtraInfo() {
        return extraInfo;
    }

    @Nullable
    @Override
    public Text getDeprecated() {
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

    @SuppressWarnings({"unchecked", "rawtypes"})
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
        if (!(manager instanceof ModuleSettingsManager moduleManager)) return null;
        return moduleManager.module;
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
        Optional<Text> error = this.validator.validate(value);
        if (error.isPresent()) throw new ParsedRule.ValueException(error.get());
        if (sync) {
            String str = typeAdapter.toString(value);
            if (options.contains(str) && !enabledOptions.contains(str)) {
                throw new ParsedRule.ValueException(t(""));
            }
        }
        if (this.fieldVolatile) {
            this.varHandle.setVolatile(value);
        } else {
            this.varHandle.set(value);
        }
        this.onChange.onChange(this, previousValue);
        //noinspection unchecked
        this.categories.forEach(c -> c.onChange((ParsedRuleImpl<Object>) this, previousValue));
        if (sync) RulesChannel.instance.sendRuleUpdate(Collections.singleton(this));
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        if (this.fieldVolatile) {
            return (T) this.varHandle.getVolatile();
        } else {
            return (T) this.varHandle.get();
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

        default List<String> getOptions() {
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

        record EnumTypeAdapter<T extends Enum<T>>(Class<T> enumClass) implements TypeAdapter<Enum<T>, String> {
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
            public List<String> getOptions() {
                return Arrays.stream(enumClass.getEnumConstants()).map(e -> e.name().toLowerCase(Locale.ROOT)).toList();
            }
        }

        TypeAdapter.Simple<String> STRING = new Simple<>(s -> s, StringArgumentType::greedyString);
        TypeAdapter.Simple<Boolean> BOOLEAN = new Simple<>(Boolean::parseBoolean, BoolArgumentType::bool) {
            @Override
            public List<String> getOptions() {
                return List.of("true", "false");
            }
        };
        TypeAdapter.Simple<Integer> INTEGER = new Simple<>(Integer::parseInt, IntegerArgumentType::integer);
        TypeAdapter.Simple<Double> DOUBLE = new Simple<>(Double::parseDouble, DoubleArgumentType::doubleArg);
    }

}
