package quickcarpet.settings;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.command.arguments.serialize.ConstantArgumentSerializer;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import quickcarpet.module.QuickCarpetModule;
import quickcarpet.network.channels.RulesChannel;
import quickcarpet.utils.Reflection;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class ParsedRule<T> implements Comparable<ParsedRule> {
    private final Map<Class<? extends Enum>, ArgumentType> ARGUMENT_TYPES = new HashMap<>();
    public final Rule rule;
    public final Field field;

    public final String shortName;
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

    ParsedRule(SettingsManager manager, Field field, Rule rule) {
        if ((field.getModifiers() & Modifier.STATIC) == 0) throw new IllegalArgumentException(field + " is not static");
        this.manager = manager;
        this.rule = rule;
        this.field = field;
        this.shortName = SettingsManager.getDefaultRuleName(field, rule);
        this.name = manager.getRuleName(field, rule);
        this.type = (Class<T>) field.getType();
        this.description = rule.desc();
        this.extraInfo = ImmutableList.copyOf(rule.extra());
        this.categories = ImmutableList.copyOf(rule.category());
        this.validator = Reflection.callPrivateConstructor(rule.validator());
        this.onChange = Reflection.callPrivateConstructor(rule.onChange());
        this.defaultValue = get();
        this.defaultAsString = convertToString(this.defaultValue);
        if (this.type == boolean.class) {
            this.options = ImmutableList.of("true", "false");
        } else if (this.type.isEnum()) {
            this.options = getEnumOptions((Class<? extends Enum>) this.type);
        } else {
            this.options = ImmutableList.copyOf(rule.options());
        }
    }

    public ArgumentType<T> getArgumentType() {
        if (type == String.class) return (ArgumentType<T>) StringArgumentType.greedyString();
        if (type == boolean.class) return (ArgumentType<T>) BoolArgumentType.bool();
        if (type == int.class) return (ArgumentType<T>) IntegerArgumentType.integer();
        if (type == double.class) return (ArgumentType<T>) DoubleArgumentType.doubleArg();
        // if (type.isEnum()) return ARGUMENT_TYPES.computeIfAbsent((Class<? extends Enum>) type, ParsedRule::createEnumArgumentType);
        if (type.isEnum()) return (ArgumentType<T>) StringArgumentType.string();
        throw new IllegalStateException("Unknown type " + type.getSimpleName());
    }

    public T getArgument(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (type.isEnum()) {
            String value = StringArgumentType.getString(context, "value");
            try {
                return (T) Enum.valueOf((Class) type, value.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().create(String.join(", ", options));
            }
        }
        return context.getArgument("value", type);
    }

    private static ImmutableList<String> getEnumOptions(Class<? extends Enum> type) {
        return Arrays.stream(type.getEnumConstants()).map(e -> ((Enum) e).name().toLowerCase(Locale.ROOT)).collect(ImmutableList.toImmutableList());
    }

    private static <T extends Enum> ArgumentType<T> createEnumArgumentType(Class<T> type) {
        ImmutableList<String> options = getEnumOptions(type);
        ArgumentType<T> arg = new ArgumentType<T>() {
            @Override
            public T parse(StringReader reader) throws CommandSyntaxException {
                int start = reader.getCursor();
                String value = reader.readUnquotedString();
                String ucValue = value.toUpperCase(Locale.ROOT);
                try {
                    return (T) Enum.valueOf(type, ucValue);
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
        String name = "carpet:" + type.getName()
                .replace(type.getPackage().getName() + ".", "")
                .replaceAll("[A-Z]", "_$0")
                .replaceAll("\\$", "")
                .toLowerCase(Locale.ROOT).substring(1);
        ArgumentTypes.register(name, (Class) arg.getClass(), new ConstantArgumentSerializer<>(() -> arg));
        return arg;
    }

    @Nullable
    public QuickCarpetModule getModule() {
        if (!(manager instanceof ModuleSettingsManager)) return null;
        return ((ModuleSettingsManager) manager).module;
    }

    public void set(String value, boolean sync) {
        if (type == String.class) {
            set((T) value, sync);
        } else if (type == boolean.class) {
            set((T) (Object) Boolean.parseBoolean(value), sync);
        } else if (type == int.class) {
            set((T) (Object) Integer.parseInt(value), sync);
        } else if (type == double.class) {
            set((T) (Object) Double.parseDouble(value), sync);
        } else if (type.isEnum()) {
            String ucValue = value.toUpperCase(Locale.ROOT);
            set((T) (Object) Enum.valueOf((Class<? extends Enum>) type, ucValue), sync);
        } else throw new IllegalStateException("Unknown type " + type.getSimpleName());
    }

    public void set(T value, boolean sync) {
        T previousValue = this.get();
        Optional<String> error = this.validator.validate(value);
        if (error.isPresent()) throw new IllegalArgumentException(error.get());
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
