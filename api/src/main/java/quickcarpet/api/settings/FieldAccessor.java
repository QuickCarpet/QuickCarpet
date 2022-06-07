package quickcarpet.api.settings;

public interface FieldAccessor<T> {
    Class<T> getType();
    T get();
    void set(T value);
}
