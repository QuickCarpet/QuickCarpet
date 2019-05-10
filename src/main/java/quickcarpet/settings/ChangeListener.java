package quickcarpet.settings;

public interface ChangeListener<T> {
    void onChange(ParsedRule<T> rule);

    class Empty implements ChangeListener {
        @Override
        public void onChange(ParsedRule rule) {}
    }
}
