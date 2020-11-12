package quickcarpet.logging.loghelpers;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class LogParameter implements Map.Entry<String, Object> {
    private final String key;
    private final Supplier<Object> supplier;

    public LogParameter(String key, Supplier<Object> supplier) {
        this.key = key;
        this.supplier = supplier;
    }

    public LogParameter(String key, Object value) {
        this(key, () -> value);
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Object getValue() {
        return supplier.get();
    }

    @Override
    public Object setValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @SafeVarargs
    public static <T> Set<Map.Entry<String, T>> parameters(Map.Entry<String, T>... params) {
        return new LinkedHashSet<>(Arrays.asList(params));
    }
}
