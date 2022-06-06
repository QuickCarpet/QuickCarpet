package quickcarpet.api.settings;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import quickcarpet.api.module.QuickCarpetModule;

import javax.annotation.Nullable;
import java.util.List;

public interface ParsedRule<T> {
    @Nullable
    QuickCarpetModule getModule();
    SettingsManager getManager();

    Rule getRule();

    FieldAccessor<T> getFieldAccessor();
    String getShortName();
    String getName();
    /**
     * @revised 2.0.0
     */
    Text getDescription();
    /**
     * @revised 2.0.0
     */
    @Nullable
    Text getExtraInfo();
    /**
     * @revised 2.0.0
     */
    @Nullable
    Text getDeprecated();
    List<RuleCategory> getCategories();
    List<String> getOptions();
    Class<T> getType();
    Validator<T> getValidator();
    ChangeListener<T> getChangeListener();
    /**
     * @since 1.1.0
     */
    boolean isDisabled();

    ArgumentType<?> getArgumentType();
    T getArgument(CommandContext<ServerCommandSource> context) throws CommandSyntaxException;

    T get();
    String getAsString();
    boolean getBoolValue();
    void set(String value, boolean sync);
    void set(T value, boolean sync);

    T getDefaultValue();
    String getDefaultAsString();
    boolean isDefault();
    void resetToDefault(boolean sync);

    T getSaved();
    String getSavedAsString();
    void save();
    boolean hasSavedValue();

    class ValueException extends IllegalArgumentException {
        public final Text message;

        /**
         * @revised 2.0.0
         */
        public ValueException(Text message) {
            this.message = message;
        }

        @Override
        public String getMessage() {
            return message.getString();
        }
    }
}
