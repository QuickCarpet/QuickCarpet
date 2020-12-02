package quickcarpet.helper;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import quickcarpet.QuickCarpet;
import quickcarpet.logging.loghelpers.LogParameter;
import quickcarpet.pubsub.PubSubInfoProvider;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static quickcarpet.utils.Messenger.*;

public class HopperCounter {
    public static final Map<Key, HopperCounter> COUNTERS;
    public static final ImmutableSet<LogParameter> COMMAND_PARAMETERS;

    static {
        EnumMap<Key, HopperCounter> counterMap = new EnumMap<>(Key.class);
        for (Key key : Key.values()) {
            counterMap.put(key, new HopperCounter(key));
        }
        COUNTERS = Maps.immutableEnumMap(counterMap);
        ImmutableSet.Builder<LogParameter> params = new ImmutableSet.Builder<>();
        for (Map.Entry<Key, HopperCounter> counterEntry : COUNTERS.entrySet()) {
            params.add(new LogParameter(counterEntry.getKey().name(), () -> counterEntry.getValue().getTotalItems()));
        }
        COMMAND_PARAMETERS = params.build();
    }

    public final Key key;
    private final Object2LongMap<Item> counter = new Object2LongLinkedOpenHashMap<>();
    private long startTick;
    private long startMillis;
    private final PubSubInfoProvider<Long> pubSubProvider;

    private HopperCounter(Key key) {
        this.key = key;
        pubSubProvider = new PubSubInfoProvider<>(QuickCarpet.PUBSUB, "carpet.counter." + key.name, 0, this::getTotalItems);
    }

    public void add(MinecraftServer server, ItemStack stack) {
        if (startTick == 0) {
            startTick = server.getTicks();
            startMillis = System.currentTimeMillis();
        }
        Item item = stack.getItem();
        counter.put(item, counter.getLong(item) + stack.getCount());
        pubSubProvider.publish();
    }

    public void reset(MinecraftServer server) {
        counter.clear();
        startTick = server.getTicks();
        startMillis = System.currentTimeMillis();
        pubSubProvider.publish();
    }

    public static void resetAll(MinecraftServer server) {
        for (HopperCounter counter : COUNTERS.values()) {
            counter.reset(server);
        }
    }

    public static List<MutableText> formatAll(MinecraftServer server, boolean realtime) {
        List<MutableText> text = new ArrayList<>();

        for (HopperCounter counter : COUNTERS.values()) {
            if (counter.getTotalItems() == 0) continue;
            List<MutableText> temp = counter.format(server, realtime, false);
            if (!text.isEmpty()) text.add(s(""));
            text.add(c(style(counter.key.getText(), Formatting.DARK_GREEN), s(":", Formatting.GRAY)));
            text.addAll(temp);
        }
        if (text.isEmpty()) {
            text.add(ts("counter.none", Formatting.GOLD));
        }
        return text;
    }

    public List<MutableText> format(MinecraftServer server, boolean realTime, boolean brief) {
        if (counter.isEmpty()) {
            if (brief) {
                return Collections.singletonList(ts("counter.format", Formatting.DARK_GREEN, key.getText(), "-", "-", "-"));
            }
            return Collections.singletonList(ts("counter.none.color", Formatting.DARK_GREEN, key.getText()));
        }
        long total = getTotalItems();
        long ticks = Math.max(realTime ? (System.currentTimeMillis() - startMillis) / 50 : server.getTicks() - startTick, 1);
        if (total == 0) {
            if (brief) {
                return Collections.singletonList(ts("counter.format", Formatting.AQUA, key.getText(), 0, 0, String.format("%.1f", ticks / 1200.0)));
            }
            MutableText line = t("counter.none.color.timed", key.getText(), String.format("%.1f", ticks / 1200.0), realTime ? c(s(" - "), t("counter.realTime")) : s(""));
            line.append(" ");
            line.append(runCommand(s("[X]", Formatting.DARK_RED, Formatting.BOLD), "/counter " + key.name + " reset", ts("counter.action.reset", Formatting.GRAY)));
        }
        if (brief) {
            return Collections.singletonList(ts("counter.format", Formatting.AQUA, key.getText(), total, total * 72000 / ticks, String.format("%.1f", ticks / 1200.0)));
        }
        return counter.object2LongEntrySet().stream().map(e -> {
            Text itemName = t(e.getKey().getTranslationKey());
            long count = e.getLongValue();
            return t("counter.format.item", itemName, count, String.format("%.1f", count * 72000.0 / ticks));
        }).collect(Collectors.toList());
    }

    public long getTotalItems() {
        return counter.values().stream().mapToLong(Long::longValue).sum();
    }

    @Nullable
    public static HopperCounter getCounter(String key) {
        return COUNTERS.get(Key.get(key));
    }

    @Nullable
    public static HopperCounter getCounter(HopperCounter.Key key) {
        return COUNTERS.get(key);
    }

    public enum Key {
        WHITE(DyeColor.WHITE),
        ORANGE(DyeColor.ORANGE),
        MAGENTA(DyeColor.MAGENTA),
        LIGHT_BLUE(DyeColor.LIGHT_BLUE),
        YELLOW(DyeColor.YELLOW),
        LIME(DyeColor.LIME),
        PINK(DyeColor.PINK),
        GRAY(DyeColor.GRAY),
        LIGHT_GRAY(DyeColor.LIGHT_GRAY),
        CYAN(DyeColor.CYAN),
        PURPLE(DyeColor.PURPLE),
        BLUE(DyeColor.BLUE),
        BROWN(DyeColor.BROWN),
        GREEN(DyeColor.GREEN),
        RED(DyeColor.RED),
        BLACK(DyeColor.BLACK),
        ;

        private static final Map<String, Key> BY_NAME;
        private static final Map<DyeColor, Key> BY_COLOR = new HashMap<>(16, 1);
        static {
            Key[] values = values();
            BY_NAME = new HashMap<>(values.length, 1);
            for (Key k : values()) {
                BY_NAME.put(k.name, k);
                if (k.color != null) BY_COLOR.put(k.color, k);
            }
        }

        private @Nullable DyeColor color;
        public final String name;
        private final String translationKey;

        Key(DyeColor color) {
            this(color.getName(), "color.minecraft." + color.getName());
            this.color = color;
        }

        Key(String name, String translationKey) {
            this.name = name;
            this.translationKey = translationKey;
        }

        @Nullable
        public DyeColor getColor() {
            return color;
        }

        public MutableText getText() {
            return t(translationKey);
        }

        public static Key get(DyeColor color) {
            return BY_COLOR.get(color);
        }

        @Nullable
        public static Key get(String name) {
            return BY_NAME.get(name);
        }
    }
}
