package quickcarpet.logging.loghelpers;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class LogParameter<T> implements Map.Entry<String, T> {
    private final String key;
    private final Supplier<T> supplier;

    public LogParameter(String key, Supplier<T> supplier) {
        this.key = key;
        this.supplier = supplier;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public T getValue() {
        return supplier.get();
    }

    @Override
    public T setValue(T value) {
        throw new UnsupportedOperationException();
    }

    @SafeVarargs
    public static <T> Set<Map.Entry<String, T>> parameters(Map.Entry<String, T>... params) {
        return new LinkedHashSet<>(Arrays.asList(params));
    }
}
