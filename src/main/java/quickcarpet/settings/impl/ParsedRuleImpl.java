package quickcarpet.settings.impl;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import quickcarpet.api.module.QuickCarpetModule;
import quickcarpet.api.settings.*;
import quickcarpet.network.channels.RulesChannel;
import quickcarpet.settings.Settings;
import quickcarpet.utils.Messenger;
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

final class ParsedRuleImpl<T> implements Comparable<ParsedRule<T>>, ParsedRule<T> {
    private final Rule rule;
    @Nullable
    private final Field field;
    private final FieldAccessor<T> fieldAccessor;

    private final String shortName;
    private final String name;
    private final TranslatableText description;
    @Nullable
    private final TranslatableText extraInfo;
    @Nullable
    private final TranslatableText deprecated;
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
        this(
            manager,
            SettingsManager.getDefaultRuleName(field.getName(), rule),
            makeFieldAccessor(field),
            Arrays.asList(rule.category()),
            Arrays.asList(rule.options()),
            Reflection.callDeprecatedPrivateConstructor(rule.validator()),
            Reflection.callDeprecatedPrivateConstructor(rule.onChange()),
            rule.deprecated(),
            rule,
            field
        );
    }

    private static VarHandle getVarHandle(Field field) {
        try {
            return MethodHandles.lookup().unreflectVarHandle(field);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static <T> VarHandleFieldAccessor<T> makeFieldAccessor(Field field) {
        int fieldAccess = field.getModifiers();
        if (((fieldAccess & (PUBLIC | STATIC | FINAL)) != (PUBLIC | STATIC))) {
            throw new IllegalArgumentException(field + " is not public static");
        }
        return (fieldAccess & VOLATILE) != 0
            ? new VolatileVarHandleFieldAccessor<>(getVarHandle(field))
            : new VarHandleFieldAccessor<>(getVarHandle(field));
    }

    static class VarHandleFieldAccessor<T> implements FieldAccessor<T> {
        protected final VarHandle varHandle;

        VarHandleFieldAccessor(VarHandle varHandle) {
            if (!varHandle.coordinateTypes().isEmpty()) {
                throw new IllegalArgumentException("VarHandle must not require coordinates");
            }
            this.varHandle = varHandle;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<T> getType() {
            return (Class<T>) varHandle.varType();
        }

        @Override
        @SuppressWarnings("unchecked")
        public T get() {
            return (T) varHandle.get();
        }

        @Override
        public void set(T value) {
            varHandle.set(value);
        }
    }

    static class VolatileVarHandleFieldAccessor<T> extends VarHandleFieldAccessor<T> {
        VolatileVarHandleFieldAccessor(VarHandle varHandle) {
            super(varHandle);
        }

        @Override
        @SuppressWarnings("unchecked")
        public T get() {
            return (T) varHandle.getVolatile();
        }

        @Override
        public void set(T value) {
            varHandle.setVolatile(value);
        }
    }

    ParsedRuleImpl(SettingsManager manager, String name, FieldAccessor<T> field, List<RuleCategory> categories, List<String> options, @Nullable Validator<T> validator, @Nullable ChangeListener<T> onChange, boolean deprecated) {
        this(
            manager,
            name,
            field,
            categories,
            options,
            validator,
            onChange,
            deprecated,
            null,
            null
        );
    }

    private ParsedRuleImpl(SettingsManager manager, String name, FieldAccessor<T> fieldAccessor, List<RuleCategory> categories, List<String> staticOptions, @Nullable Validator<T> validator, @Nullable ChangeListener<T> onChange, boolean deprecated, @Nullable Rule rule, @Nullable Field field) {
        this.manager = manager;
        this.rule = rule;
        this.field = field;
        this.fieldAccessor = fieldAccessor;
        this.shortName = name;
        this.name = manager.getRuleName(name, rule);
        this.type = fieldAccessor.getType();
        this.description = new TranslatableText(manager.getDescriptionTranslationKey(name, rule));
        String extraKey = manager.getExtraTranslationKey(name, rule);
        this.extraInfo = Translations.hasTranslation(extraKey) ? new TranslatableText(extraKey) : null;
        this.categories = ImmutableList.copyOf(categories);
        this.validator = validator == null ? v -> Optional.empty() : validator;
        this.onChange = onChange == null ? (r, p) -> {} : onChange;
        this.typeAdapter = getTypeAdapter(this.type);
        this.defaultValue = get();
        this.defaultAsString = typeAdapter.toString(this.defaultValue);
        List<String> options = typeAdapter.getOptions();
        if (options == null) options = ImmutableList.copyOf(staticOptions);
        this.options = options;
        boolean disabled = !isEnabled(this);
        this.enabledOptions = getEnabledOptions(this, options, disabled);
        this.disabled = disabled || (!options.isEmpty() && enabledOptions.size() <= 1);
        this.deprecated = deprecated ? new TranslatableText(manager.getDeprecationTranslationKey(name, rule)) : null;
    }

    private static boolean isEnabled(ParsedRuleImpl<?> rule) {
        return ((SettingsManager) rule.manager).source.isRuleEnabled(rule);
    }

    private static List<String> getEnabledOptions(ParsedRuleImpl<?> rule, List<String> options, boolean disabled) {
        return disabled ? List.of(rule.getDefaultAsString()) : ((SettingsManager) rule.manager).source.getEnabledOptions(rule, options);
    }

    @Override
    public Rule getRule() {
        return rule;
    }

    @Override
    @Nullable
    public Field getField() {
        return field;
    }

    @Override
    public FieldAccessor<T> getFieldAccessor() {
        return fieldAccessor;
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
        return manager instanceof ModuleSettingsManager moduleManager ? moduleManager.getModule() : null;
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
                throw new ParsedRule.ValueException(Messenger.t(""));
            }
        }
        this.fieldAccessor.set(value);
        this.onChange.onChange(this, previousValue);
        //noinspection unchecked
        this.categories.forEach(c -> c.onChange((ParsedRuleImpl<Object>) this, previousValue));
        if (sync) RulesChannel.instance.sendRuleUpdate(Collections.singleton(this));
    }

    @Override
    public T get() {
        return this.fieldAccessor.get();
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
