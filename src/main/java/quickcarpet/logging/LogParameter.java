package quickcarpet.logging;

import java.util.Map;
import java.util.function.Supplier;

public record LogParameter(String key, Supplier<Object> supplier) implements Map.Entry<String, Object> {
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
}
