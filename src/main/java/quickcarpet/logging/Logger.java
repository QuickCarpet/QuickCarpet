package quickcarpet.logging;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Lazy;
import quickcarpet.QuickCarpetServer;
import quickcarpet.logging.loghelpers.LogParameter;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Logger implements Comparable<Logger> {
    boolean active = false;
    @Nullable
    private Text unavailable;

    private final String name;
    private final MutableText displayName;
    private final String[] options;
    private final String defaultOption;
    final LogHandler defaultHandler;

    public Logger(String name, String def, String[] options, LogHandler defaultHandler) {
        this.name = name;
        this.displayName = new LiteralText(name);
        displayName.setStyle(displayName.getStyle().withColor(Formatting.GOLD));
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
    public int compareTo(Logger o) {
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

    public void setUnavailable(MutableText reason) {
        this.unavailable = reason;
    }

    public boolean isAvailable() {
        return this.unavailable == null;
    }

    @Nullable
    public MutableText getUnavailabilityReason() {
        return isAvailable() ? null : unavailable.copy();
    }

    /**
     * serves messages to players fetching them from the promise
     * will repeat invocation for players that share the same option
     */
    @FunctionalInterface
    public interface MessageSupplier {
        MutableText[] get(String playerOption, PlayerEntity player);
    }

    public void log(MessageSupplier message) {
        this.log(message, () -> null);
    }

    public void log(MessageSupplier message, Supplier<Collection<LogParameter>> commandParams) {
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
        MutableText[] get(String playerOption);
        default MutableText[] get(String playerOption, PlayerEntity player) {
            return get(playerOption);
        }
    }

    public void log(PlayerIndependentMessageSupplier message) {
        this.log(message, () -> null);
    }

    public void log(PlayerIndependentMessageSupplier message, Supplier<Collection<LogParameter>>  commandParams) {
        Map<String, MutableText[]> messages = new HashMap<>();
        getOnlineSubscribers().forEach(player -> sendMessage(player, messages.computeIfAbsent(getOption(player), message::get), commandParams));
    }

    public void log(Supplier<MutableText[]> message) {
        this.log(message, () -> null);
    }

    public void log(Supplier<MutableText[]> message, Supplier<Collection<LogParameter>>  commandParams) {
        Lazy<MutableText[]> messages = new Lazy<>(message);
        getOnlineSubscribers().forEach(player -> sendMessage(player, messages.get(), commandParams));
    }

    private static LoggerManager getManager() {
        QuickCarpetServer server = QuickCarpetServer.getNullableInstance();
        return server == null ? null : server.loggers;
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

    private void sendMessage(ServerPlayerEntity player, MutableText[] messages, Supplier<Collection<LogParameter>> commandParams) {
        if (messages == null) return;
        Supplier<Map<String, Object>> params = () -> {
            ImmutableMap.Builder<String, Object> builder = new ImmutableMap.Builder<>();
            for (LogParameter p : commandParams.get()) builder.put(p);
            return builder.build();
        };
        getHandler(player).handle(this, player, messages, params);
    }
}
