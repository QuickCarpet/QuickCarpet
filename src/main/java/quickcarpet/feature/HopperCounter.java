package quickcarpet.feature;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HopperBlock;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import quickcarpet.QuickCarpet;
import quickcarpet.logging.LogParameter;
import quickcarpet.pubsub.PubSubInfoProvider;
import quickcarpet.utils.Constants.Counter.Keys;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static quickcarpet.utils.Constants.Counter.Texts.*;
import static quickcarpet.utils.Messenger.*;

public class HopperCounter {
    public static final Map<Key, HopperCounter> COUNTERS;
    public static final ImmutableSet<LogParameter> COMMAND_PARAMETERS;
    private static HopperCounter.Combined combined = null;

    static {
        EnumMap<Key, HopperCounter> counterMap = new EnumMap<>(Key.class);
        for (Key key : Key.values()) {
            counterMap.put(key, key.createCounter());
        }
        COUNTERS = Maps.immutableEnumMap(counterMap);
        ImmutableSet.Builder<LogParameter> params = new ImmutableSet.Builder<>();
        for (Map.Entry<Key, HopperCounter> counterEntry : COUNTERS.entrySet()) {
            params.add(new LogParameter(counterEntry.getKey().name(), () -> counterEntry.getValue().getTotalItems()));
        }
        COMMAND_PARAMETERS = params.build();
    }

    public final Key key;
    protected final Object2LongMap<Item> counter = new Object2LongLinkedOpenHashMap<>();
    protected long startTick;
    protected long startMillis;
    protected final PubSubInfoProvider<Long> pubSubProvider;

    private HopperCounter(Key key) {
        this.key = key;
        pubSubProvider = new PubSubInfoProvider<>(QuickCarpet.PUBSUB, "carpet.counter." + key.name, 0, this::getTotalItems);
    }

    public void add(MinecraftServer server, ItemStack stack) {
        add(server, stack.getItem(), stack.getCount());
    }

    public void add(MinecraftServer server, Item item, int count) {
        if (startTick == 0) {
            startTick = server.getTicks();
            startMillis = System.currentTimeMillis();
        }
        counter.put(item, counter.getLong(item) + count);
        pubSubProvider.publish();
        combined.update();
    }

    public static boolean tryCount(World world, BlockPos pos, BlockState state, Inventory hopper, HopperCounter from) {
        HopperCounter to = COUNTERS.get(Key.getCounterKey(world, pos.offset(state.get(HopperBlock.FACING))));
        if (to == null) return false;
        for (int i = 0; i < hopper.size(); ++i) {
            if (!hopper.getStack(i).isEmpty()) {
                ItemStack stack = hopper.getStack(i);
                to.add(world.getServer(), stack);
                if (from == null) {
                    hopper.setStack(i, ItemStack.EMPTY);
                } else {
                    from.add(world.getServer(), stack.getItem(), -stack.getCount());
                }
            }
        }
        return true;
    }

    public void reset(MinecraftServer server) {
        counter.clear();
        startTick = server.getTicks();
        startMillis = System.currentTimeMillis();
        pubSubProvider.publish();
        combined.update();
    }

    public static Collection<Text> formatAll(MinecraftServer server, boolean realtime) {
        List<Text> text = new ArrayList<>();

        for (HopperCounter counter : COUNTERS.values()) {
            if (counter.getTotalItems() == 0 || counter.key == Key.ALL) continue;
            List<MutableText> temp = counter.format(server, realtime, false);
            if (!text.isEmpty()) text.add(s(""));
            text.add(c(style(counter.key.getText(), Formatting.DARK_GREEN), s(":", Formatting.GRAY)));
            text.addAll(temp);
        }
        if (text.isEmpty()) {
            text.add(NONE);
        }
        return text;
    }

    public List<MutableText> format(MinecraftServer server, boolean realTime, boolean brief) {
        if (counter.isEmpty()) {
            if (brief) {
                return Collections.singletonList(ts(Keys.FORMAT, Formatting.DARK_GREEN, key.getText(), "-", "-", "-"));
            }
            return Collections.singletonList(ts(Keys.NONE_COLOR, Formatting.DARK_GREEN, key.getText()));
        }
        long total = getTotalItems();
        long ticks = Math.max(realTime ? (System.currentTimeMillis() - startMillis) / 50 : server.getTicks() - startTick, 1);
        if (total == 0) {
            if (brief) {
                return Collections.singletonList(ts(Keys.FORMAT, Formatting.AQUA, key.getText(), 0, 0, String.format("%.1f", ticks / 1200.0)));
            }
            MutableText line = t(Keys.NONE_COLOR_TIMED, key.getText(), String.format("%.1f", ticks / 1200.0), realTime ? c(s(" - "), REAL_TIME) : s(""));
            line.append(" ");
            line.append(runCommand(s("[X]", Formatting.DARK_RED, Formatting.BOLD), "/counter " + key.name + " reset", ACTION_RESET));
        }
        if (brief) {
            return Collections.singletonList(ts(Keys.FORMAT, Formatting.AQUA, key.getText(), total, total * 72000 / ticks, String.format("%.1f", ticks / 1200.0)));
        }
        return counter.object2LongEntrySet().stream().map(e -> {
            Text itemName = t(e.getKey().getTranslationKey());
            long count = e.getLongValue();
            return t(Keys.FORMAT_ITEM, itemName, count, String.format("%.1f", count * 72000.0 / ticks));
        }).collect(Collectors.toList());
    }

    public long getTotalItems() {
        return counter.values().longStream().sum();
    }

    private static class Combined extends HopperCounter {
        private Combined(Key key) {
            super(key);
            combined = this;
        }

        @Override
        public void add(MinecraftServer server, ItemStack stack) {
            throw new UnsupportedOperationException("Don't add items to the 'all' counter");
        }

        @Override
        public void reset(MinecraftServer server) {
            for (HopperCounter c : COUNTERS.values()) {
                if (c == this) continue;
                c.reset(server);
            }
        }

        public void update() {
            counter.clear();
            startTick = Long.MAX_VALUE;
            startMillis = Long.MAX_VALUE;
            for (HopperCounter c : COUNTERS.values()) {
                if (c == this) continue;
                startTick = Math.min(startTick, c.startTick);
                startMillis = Math.min(startMillis, c.startMillis);
                for (Object2LongMap.Entry<Item> e : c.counter.object2LongEntrySet()) {
                    counter.put(e.getKey(), counter.getLong(e.getKey()) + e.getLongValue());
                }
            }
            pubSubProvider.publish();
        }
    }

    @Nullable
    public static HopperCounter getCounter(String key) {
        return COUNTERS.get(Key.get(key));
    }

    @Nonnull
    public static HopperCounter getCounter(HopperCounter.Key key) {
        return COUNTERS.get(key);
    }

    public enum Key {
        WHITE(DyeColor.WHITE, Blocks.WHITE_WOOL),
        ORANGE(DyeColor.ORANGE, Blocks.ORANGE_WOOL),
        MAGENTA(DyeColor.MAGENTA, Blocks.MAGENTA_WOOL),
        LIGHT_BLUE(DyeColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_WOOL),
        YELLOW(DyeColor.YELLOW, Blocks.YELLOW_WOOL),
        LIME(DyeColor.LIME, Blocks.LIME_WOOL),
        PINK(DyeColor.PINK, Blocks.PINK_WOOL),
        GRAY(DyeColor.GRAY, Blocks.GRAY_WOOL),
        LIGHT_GRAY(DyeColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_WOOL),
        CYAN(DyeColor.CYAN, Blocks.CYAN_WOOL),
        PURPLE(DyeColor.PURPLE, Blocks.PURPLE_WOOL),
        BLUE(DyeColor.BLUE, Blocks.BLUE_WOOL),
        BROWN(DyeColor.BROWN, Blocks.BROWN_WOOL),
        GREEN(DyeColor.GREEN, Blocks.GREEN_WOOL),
        RED(DyeColor.RED, Blocks.RED_WOOL),
        BLACK(DyeColor.BLACK, Blocks.BLACK_WOOL),
        CACTUS("cactus", Items.CACTUS.getTranslationKey()),
        DESPAWN("despawn", "counter.key.despawn"),
        DESTROY("destroy", "counter.key.destroy"),
        PICKUP("pickup", "stat_type.minecraft.picked_up"),
        ALL("all", "gui.all", Combined::new)
        ;

        private static final Map<String, Key> BY_NAME;
        private static final Map<DyeColor, Key> BY_COLOR = new HashMap<>(16, 1);
        private static final Map<Block, Key> BY_BLOCK = new HashMap<>(16, 1);
        static {
            Key[] values = values();
            BY_NAME = new HashMap<>(values.length, 1);
            for (Key k : values()) {
                BY_NAME.put(k.name, k);
                if (k.color != null) BY_COLOR.put(k.color, k);
                if (k.block != null) BY_BLOCK.put(k.block, k);
            }
        }

        private @Nullable DyeColor color;
        private @Nullable Block block;
        public final String name;
        private final String translationKey;
        private final Function<Key, HopperCounter> creator;

        Key(DyeColor color, Block block) {
            this(color.getName(), "color.minecraft." + color.getName());
            this.color = color;
            this.block = block;
        }

        Key(String name, String translationKey) {
            this(name, translationKey, HopperCounter::new);
        }

        Key(String name, String translationKey, Function<Key, HopperCounter> creator) {
            this.name = name;
            this.translationKey = translationKey;
            this.creator = creator;
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

        @Nullable
        public static Key get(Block block) {
            return BY_BLOCK.get(block);
        }

        @Nullable
        public static Key getCounterKey(World world, BlockPos pos) {
            return get(world.getBlockState(pos).getBlock());
        }

        public HopperCounter createCounter() {
            return creator.apply(this);
        }
    }
}
