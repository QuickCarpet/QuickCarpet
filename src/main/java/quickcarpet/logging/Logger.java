package quickcarpet.logging;

import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Lazy;
import quickcarpet.QuickCarpet;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Logger<T extends Logger.CommandParameters> implements Comparable<Logger<?>> {
    boolean active = false;
    @Nullable
    private Text unavailable;

    private final String name;
    private final Text displayName;
    private final String[] options;
    private final String defaultOption;
    final LogHandler defaultHandler;

    public Logger(String name, String def, String[] options, LogHandler defaultHandler) {
        this.name = name;
        this.displayName = new LiteralText(name);
        displayName.getStyle().setColor(Formatting.GOLD);
        this.defaultOption = def;
        this.options = options == null ? new String[0] : options;
        this.defaultHandler = defaultHandler;
    }

    public String getDefault() {
        return defaultOption;
    }

    public String[] getOptions() {
        return options;
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(Logger<?> o) {
        return name.compareTo(o.name);
    }

    public boolean isActive() {
        return active;
    }

    public Text getDisplayName() {
        return displayName;
    }

    public void setAvailable() {
        this.unavailable = null;
    }

    public void setUnavailable(Text reason) {
        this.unavailable = reason;
    }

    public boolean isAvailable() {
        return this.unavailable == null;
    }

    @Nullable
    public Text getUnavailabilityReason() {
        return isAvailable() ? null : unavailable.copy();
    }

    /**
     * serves messages to players fetching them from the promise
     * will repeat invocation for players that share the same option
     */
    @FunctionalInterface
    public interface MessageSupplier {
        Text[] get(String playerOption, PlayerEntity player);
    }

    public void log(MessageSupplier message) {
        this.log(message, (Supplier<T>) EmptyCommandParameters.SUPPLIER);
    }

    public void log(MessageSupplier message, Supplier<T> commandParams) {
        getOnlineSubscribers().forEach(player -> {
            sendMessage(player, message.get(getOption(player), player), commandParams);
        });
    }

    /**
     * guarantees that each message for each option will be evaluated once from the promise
     * and served the same way to all other players subscribed to the same option
     */
    @FunctionalInterface
    public interface PlayerIndependentMessageSupplier extends MessageSupplier {
        Text[] get(String playerOption);
        default Text[] get(String playerOption, PlayerEntity player) {
            return get(playerOption);
        }
    }

    public void log(PlayerIndependentMessageSupplier message) {
        this.log(message, (Supplier<T>) EmptyCommandParameters.SUPPLIER);
    }

    public void log(PlayerIndependentMessageSupplier message, Supplier<T>  commandParams) {
        Map<String, Text[]> messages = new HashMap<>();
        getOnlineSubscribers().forEach(player -> sendMessage(player, messages.computeIfAbsent(getOption(player), message::get), commandParams));
    }

    public void log(Supplier<Text[]> message) {
        this.log(message, (Supplier<T>) EmptyCommandParameters.SUPPLIER);
    }

    public void log(Supplier<Text[]> message, Supplier<T>  commandParams) {
        Lazy<Text[]> messages = new Lazy<>(message);
        getOnlineSubscribers().forEach(player -> sendMessage(player, messages.get(), commandParams));
    }

    private static LoggerManager getManager() {
        return QuickCarpet.getInstance().loggers;
    }

    private String getOption(ServerPlayerEntity player) {
        return getManager().getPlayerSubscriptions(player.getEntityName()).getOption(this);
    }

    private LogHandler getHandler(ServerPlayerEntity player) {
        return getManager().getPlayerSubscriptions(player.getEntityName()).getHandler(this);
    }

    private Stream<ServerPlayerEntity> getOnlineSubscribers() {
        LoggerManager manager = getManager();
        if (manager == null) return Stream.empty();
        return manager.getOnlineSubscribers(this);
    }

    private void sendMessage(ServerPlayerEntity player, Text[] messages, Supplier<T> commandParams) {
        if (messages == null) return;
        //noinspection unchecked
        getHandler(player).handle(this, player, messages, (Supplier) commandParams);
    }

    public interface CommandParameters<T> extends Map<String, T> {}

    public static class EmptyCommandParameters extends Object2ObjectMaps.EmptyMap<String, Object> implements CommandParameters<Object> {
        public static final EmptyCommandParameters INSTANCE = new EmptyCommandParameters();
        public static final Supplier<EmptyCommandParameters> SUPPLIER = () -> INSTANCE;
        private EmptyCommandParameters() {}
    }
}
