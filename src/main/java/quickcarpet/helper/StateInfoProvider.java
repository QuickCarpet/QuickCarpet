package quickcarpet.helper;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.State;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import quickcarpet.utils.Messenger;
import quickcarpet.utils.Messenger.Formatter;

import javax.annotation.Nonnull;
import java.util.*;

import static quickcarpet.utils.Messenger.*;

public interface StateInfoProvider<S extends State<?, S>, T extends Comparable<T>> {
    T get(S state, ServerWorld world, BlockPos pos);

    default Formatter<T> getFormatter(@Nonnull T value) {
        //noinspection unchecked
        return (Formatter<T>) getDefaultFormatter(value.getClass());
    }

    default MutableText getAndFormat(S state, ServerWorld world, BlockPos pos) {
        return format(get(state, world, pos));
    }

    default MutableText format(T value) {
        return getFormatter(value).format(value);
    }

    @SuppressWarnings("unchecked")
    static <T> Formatter<T> getDefaultFormatter(Class<T> type) {
        if (type == Double.class || type == Float.class) return (Formatter<T>) Formatter.FLOAT;
        if (Number.class.isAssignableFrom(type)) return (Formatter<T>) Formatter.NUMBER;
        if (type == Boolean.class) return (Formatter<T>) Formatter.BOOLEAN;
        return value -> s(String.valueOf(value));
    }

    interface Directional<S extends State<?, S>, T extends Comparable<T>> extends StateInfoProvider<S, T> {
        default T get(S state, ServerWorld world, BlockPos pos) {
            T value = null;
            for (Direction d : Direction.values()) {
                T dValue = get(state, world, pos, d);
                if (value == null || dValue.compareTo(value) > 0) value = dValue;
            }
            return value;
        }

        @SuppressWarnings("UnstableApiUsage")
        default MutableText getAndFormat(S state, ServerWorld world, BlockPos pos) {
            Multimap<T, Direction> values = MultimapBuilder.treeKeys().linkedHashSetValues().build();
            for (Direction d : Direction.values()) values.put(get(state, world, pos, d), d);
            Set<T> keys = values.keySet();
            switch (keys.size()) {
                case 1 -> {
                    T value = keys.iterator().next();
                    return format(value);
                }
                case 2 -> {
                    Iterator<T> it = keys.iterator();
                    T value1 = it.next();
                    T value2 = it.next();
                    Collection<Direction> directions1 = values.get(value1);
                    Collection<Direction> directions2 = values.get(value2);
                    if (directions1.size() > directions2.size()) {
                        T temp = value1;
                        value1 = value2;
                        value2 = temp;
                        directions1 = directions2;
                    }
                    MutableText format1 = format(value1);
                    MutableText format2 = format(value2);
                    MutableText directions = directions1.stream().map(Messenger::format).collect(joining(s(",")));
                    return t("state_info_provider.2", directions, format1, format2);
                }
                default -> {
                    List<Text> parts = new ArrayList<>(keys.size());
                    for (T key : keys) {
                        MutableText part = values.get(key).stream().map(Messenger::format).collect(joining(s(",")));
                        parts.add(part.append(": ").append(format(key)));
                    }
                    return join(s(", "), parts);
                }
            }
        }

        T get(S state, ServerWorld world, BlockPos pos, Direction direction);
    }

    class WithFormatter<S extends State<?, S>, T extends Comparable<T>> implements Directional<S, T> {
        private final StateInfoProvider<S, T> provider;
        private final Formatter<T> formatter;

        public WithFormatter(StateInfoProvider<S, T> provider, Formatter<T> formatter) {
            this.provider = provider;
            this.formatter = formatter;
        }

        @Override
        public Formatter<T> getFormatter(@Nonnull T value) {
            return formatter;
        }

        @Override
        public T get(S state, ServerWorld world, BlockPos pos) {
            return provider.get(state, world, pos);
        }

        @Override
        public T get(S state, ServerWorld world, BlockPos pos, Direction direction) {
            if (provider instanceof Directional) return (((Directional<S, T>) provider).get(state, world, pos, direction));
            return provider.get(state, world, pos);
        }
    }
}
