package quickcarpet.helper;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import quickcarpet.QuickCarpet;
import quickcarpet.logging.Logger;
import quickcarpet.pubsub.PubSubInfoProvider;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static quickcarpet.utils.Messenger.*;

public class HopperCounter
{
    public static final Map<DyeColor, HopperCounter> COUNTERS;

    static {
        EnumMap<DyeColor, HopperCounter> counterMap = new EnumMap<>(DyeColor.class);
        for (DyeColor color : DyeColor.values()) {
            counterMap.put(color, new HopperCounter(color));
        }
        COUNTERS = Maps.immutableEnumMap(counterMap);
    }

    public final DyeColor color;
    private final Object2LongMap<Item> counter = new Object2LongLinkedOpenHashMap<>();
    private long startTick;
    private long startMillis;
    private PubSubInfoProvider<Long> pubSubProvider;

    private HopperCounter(DyeColor color) {
        this.color = color;
        pubSubProvider = new PubSubInfoProvider<>(QuickCarpet.PUBSUB, "carpet.counter." + color.getName(), 0, this::getTotalItems);
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

    public static List<Text> formatAll(MinecraftServer server, boolean realtime)
    {
        List<Text> text = new ArrayList<>();

        for (HopperCounter counter : COUNTERS.values()) {
            if (counter.getTotalItems() == 0) continue;
            List<Text> temp = counter.format(server, realtime, false);
            if (!text.isEmpty()) text.add(s(""));
            text.add(c(style(counter.getColorText(), DARK_GREEN), s(":", GRAY)));
            text.addAll(temp);
        }
        if (text.isEmpty()) {
            text.add(ts("counter.none", GOLD));
        }
        return text;
    }

    private TranslatableText getColorText() {
        return t("color.minecraft." + color.getName());
    }

    public List<Text> format(MinecraftServer server, boolean realTime, boolean brief) {
        if (counter.isEmpty()) {
            if (brief) {
                return Collections.singletonList(ts("counter.format", DARK_GREEN, getColorText(), "-", "-", "-"));
            }
            return Collections.singletonList(ts("counter.none.color", DARK_GREEN, getColorText()));
        }
        long total = getTotalItems();
        long ticks = Math.max(realTime ? (System.currentTimeMillis() - startMillis) / 50 : server.getTicks() - startTick, 1);
        if (total == 0) {
            if (brief) {
                return Collections.singletonList(ts("counter.format", CYAN, getColorText(), 0, 0, String.format("%.1f", ticks / 1200.0)));
            }
            Text line = t("counter.none.color.timed", getColorText(), String.format("%.1f", ticks / 1200.0), realTime ? c(s(" - "), t("counter.realTime")) : s(""));
            line.append(" ");
            line.append(runCommand(s("[X]", "nb"), "/counter " + color.getName() + " reset", ts("counter.action.reset", GRAY)));
        }
        if (brief) {
            return Collections.singletonList(ts("counter.format", CYAN, getColorText(), total, total * 72000 / ticks, String.format("%.1f", ticks / 1200.0)));
        }
        return counter.object2LongEntrySet().stream().map(e -> {
            Text itemName = t(e.getKey().getTranslationKey());
            long count = e.getLongValue();
            return t("counter.format.item", itemName, count, String.format("%.1f", count * 72000.0 / ticks));
        }).collect(Collectors.toList());
    }

    @Nullable
    public static HopperCounter getCounter(String color) {
        try {
            DyeColor colorEnum = DyeColor.valueOf(color.toUpperCase(Locale.ROOT));
            return COUNTERS.get(colorEnum);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public long getTotalItems() {
        return counter.values().stream().mapToLong(Long::longValue).sum();
    }

    public static class LogCommandParameters extends AbstractMap<String, Long> implements Logger.CommandParameters<Long> {
        public static final LogCommandParameters INSTANCE = new LogCommandParameters();
        private LogCommandParameters() {}
        @Override
        public Set<Entry<String, Long>> entrySet() {
            Map<String, Long> counts = new LinkedHashMap<>();
            for (Entry<DyeColor, HopperCounter> counterEntry : COUNTERS.entrySet()) {
                counts.put(counterEntry.getKey().name(), counterEntry.getValue().getTotalItems());
            }
            return counts.entrySet();
        }
    }
}
