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
import quickcarpet.logging.source.LoggerSource;
import quickcarpet.utils.QuickCarpetIdentifier;
import quickcarpet.utils.QuickCarpetRegistries;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class Logger implements Comparable<Logger> {
    public static Codec<Logger> NAME_CODEC = QuickCarpetIdentifier.CODEC.comapFlatMap(Loggers::getDataResult, Logger::getId).stable();

    boolean active = false;
    private @Nullable BiConsumer<MutableText, Collection<LogParameter>> testListener;


    private final @Nullable String[] options;
    private final @Nullable String defaultOption;
    final @Nullable LogHandler defaultHandler;
    private @Nullable Supplier<MutableText> unavailabilityReason;
    private @Nullable Supplier<LoggerSource> sourceCreator;

    public Logger(
        @Nullable String def,
        @Nullable String[] options,
        @Nullable LogHandler defaultHandler,
        @Nullable Supplier<MutableText> unavailabilityReason,
        @Nullable Supplier<LoggerSource> source
    ) {
        this.defaultOption = def;
        this.options = options == null ? new String[0] : options;
        this.defaultHandler = defaultHandler;
        this.unavailabilityReason = unavailabilityReason;
        this.sourceCreator = source;
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

    @Nullable
    public LoggerSource createSource() {
        return this.sourceCreator == null ? null : this.sourceCreator.get();
    }

    public void setAvailable() {
        this.unavailabilityReason = null;
    }

    public void setUnavailable(MutableText reason) {
        this.unavailabilityReason = reason::copy;
    }

    public boolean isAvailable() {
        return getUnavailabilityReason() == null;
    }

    @Nullable
    public MutableText getUnavailabilityReason() {
        return this.unavailabilityReason != null ? this.unavailabilityReason.get() : null;
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
        forEachSubscription((player, option) -> sendMessage(player, message.get(option, player), commandParams));
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
        forEachSubscription((player, option) -> sendMessage(player, messages.computeIfAbsent(option, message::get), commandParams));
        if (testListener != null) testListener.accept(messages.computeIfAbsent(getDefault(), message::get), commandParams.get());
    }

    public void log(Supplier<MutableText> message) {
        this.log(message, () -> null);
    }

    public void log(Supplier<MutableText> message, Supplier<Collection<LogParameter>>  commandParams) {
        Lazy<MutableText> messages = new Lazy<>(message);
        forEachSubscription((player, option) -> sendMessage(player, messages.get(), commandParams));
        if (testListener != null) testListener.accept(messages.get(), commandParams.get());
    }

    private static LoggerManager getManager() {
        QuickCarpetServer server = QuickCarpetServer.getNullableInstance();
        return server == null ? null : server.loggers;
    }

    private void forEachSubscription(BiConsumer<ServerPlayerEntity, String> action) {
        LoggerManager manager = getManager();
        if (manager == null) return;
        LoggerSource source = manager.getSource(this);
        manager.getOnlineSubscribers(this).forEachOrdered(player -> {
            String optionString = manager.getPlayerSubscriptions(player.getEntityName()).getOption(this);
            List<String> options = source == null || optionString == null ? Collections.singletonList(optionString) : source.parseOptions(optionString);
            for (String option : options) {
                action.accept(player, option);
            }
        });
    }

    private LogHandler getHandler(ServerPlayerEntity player) {
        return getManager().getPlayerSubscriptions(player.getEntityName()).getHandler(this);
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

    public static class Builder {
        private LogHandler defaultHandler;
        private List<String> options;
        private String defaultOption;
        private Supplier<MutableText> unavailabilityReason;
        private Supplier<LoggerSource> source;

        public Builder withDefaultHandler(LogHandler handler) {
            this.defaultHandler = handler;
            return this;
        }

        public Builder withOptions(List<String> options) {
            this.options = options;
            this.defaultOption = options.get(0);
            return this;
        }

        public Builder withOptions(String ...options) {
            return withOptions(Arrays.asList(options));
        }

        public Builder withDefaultOption(String option) {
            this.defaultOption = option;
            return this;
        }

        public Builder withUnavailabilityReason(Supplier<MutableText> reason) {
            this.unavailabilityReason = reason;
            return this;
        }

        public Builder withSource(Supplier<LoggerSource> source) {
            this.source = source;
            return this;
        }

        public Logger build() {
            return new Logger(defaultOption, options == null ? null : options.toArray(new String[0]), defaultHandler, unavailabilityReason, source);
        }
    }
}
