package quickcarpet.logging;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Lazy;
import org.jetbrains.annotations.VisibleForTesting;
import quickcarpet.QuickCarpetServer;
import quickcarpet.utils.QuickCarpetIdentifier;
import quickcarpet.utils.QuickCarpetRegistries;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Logger implements Comparable<Logger> {
    public static Codec<Logger> NAME_CODEC = QuickCarpetIdentifier.CODEC.comapFlatMap(Loggers::getDataResult, Logger::getId).stable();

    boolean active = false;
    private @Nullable BiConsumer<MutableText, Collection<LogParameter>> testListener;
    private @Nullable Text unavailable;

    private final String[] options;
    private final String defaultOption;
    final LogHandler defaultHandler;

    public Logger(String def, String[] options, LogHandler defaultHandler) {
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

    public Identifier getId() {
        return QuickCarpetRegistries.LOGGER.getId(this);
    }

    @Override
    public int compareTo(Logger o) {
        return getId().compareTo(o.getId());
    }

    public boolean isActive() {
        return active && isAvailable();
    }

    public Text getDisplayName() {
        return new LiteralText(QuickCarpetIdentifier.toString(getId())).formatted(Formatting.GOLD);
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
        MutableText get(String playerOption, PlayerEntity player);
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
        MutableText get(String playerOption);
        default MutableText get(String playerOption, PlayerEntity player) {
            return get(playerOption);
        }
    }

    public void log(PlayerIndependentMessageSupplier message) {
        this.log(message, () -> null);
    }

    public void log(PlayerIndependentMessageSupplier message, Supplier<Collection<LogParameter>>  commandParams) {
        Map<String, MutableText> messages = new HashMap<>();
        getOnlineSubscribers().forEach(player -> sendMessage(player, messages.computeIfAbsent(getOption(player), message::get), commandParams));
        if (testListener != null) testListener.accept(messages.computeIfAbsent(getDefault(), message::get), commandParams.get());
    }

    public void log(Supplier<MutableText> message) {
        this.log(message, () -> null);
    }

    public void log(Supplier<MutableText> message, Supplier<Collection<LogParameter>>  commandParams) {
        Lazy<MutableText> messages = new Lazy<>(message);
        getOnlineSubscribers().forEach(player -> sendMessage(player, messages.get(), commandParams));
        if (testListener != null) testListener.accept(messages.get(), commandParams.get());
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

    private void sendMessage(ServerPlayerEntity player, MutableText message, Supplier<Collection<LogParameter>> commandParams) {
        if (message == null) return;
        Supplier<Map<String, Object>> params = () -> {
            ImmutableMap.Builder<String, Object> builder = new ImmutableMap.Builder<>();
            for (LogParameter p : commandParams.get()) builder.put(p);
            return builder.build();
        };
        getHandler(player).handle(this, player, message, params);
    }

    @VisibleForTesting
    public Runnable test(BiConsumer<MutableText, Map<String, Object>> listener) {
        boolean activeBefore = active;
        active = true;
        testListener = (msg, params) -> {
            Map<String, Object> paramMap = new LinkedHashMap<>();
            if (params != null) for (LogParameter param : params) paramMap.put(param.key(), param.getValue());
            listener.accept(msg, paramMap);
        };
        return () -> {
            testListener = null;
            active = activeBefore;
        };
    }
}
