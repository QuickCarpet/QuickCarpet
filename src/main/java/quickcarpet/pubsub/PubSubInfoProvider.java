package quickcarpet.pubsub;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class PubSubInfoProvider<T> implements Supplier<T> {
    public final PubSubNode node;
    public final int interval;
    private final Supplier<T> supplier;
    private final Function<PubSubInfoProvider, T> function;
    private int phase;
    private Optional<T> previous = Optional.empty();
    private boolean publishAlways = true;

    public PubSubInfoProvider(PubSubManager manager, String node, int interval, Supplier<T> supplier) {
        this(manager.getOrCreateNode(node), interval, supplier);
    }

    public PubSubInfoProvider(PubSubManager manager, String node, int interval, Function<PubSubInfoProvider, T> function) {
        this(manager.getOrCreateNode(node), interval, function);
    }

    public PubSubInfoProvider(PubSubNode node, int interval, Function<PubSubInfoProvider, T> function) {
        this.node = node;
        this.interval = interval;
        this.supplier = null;
        this.function = function;
        node.provider = this;
    }

    public PubSubInfoProvider(PubSubNode node, int interval, Supplier<T> supplier) {
        this.node = node;
        this.interval = interval;
        this.supplier = supplier;
        this.function = null;
        node.provider = this;
    }

    public PubSubInfoProvider<T> setPhase(int phase) {
        this.phase = phase;
        return this;
    }

    public PubSubInfoProvider<T> setPublishAlways(boolean always) {
        this.publishAlways = always;
        return this;
    }

    public boolean shouldUpdate(int tickCounter) {
        if (interval == 0) return false;
        return tickCounter % interval == phase;
    }

    public void publish() {
        T newValue = this.get();
        if (publishAlways || !previous.isPresent() || !Objects.equals(previous.get(), newValue)) {
            this.node.publish(newValue);
        }
        previous = Optional.of(newValue);
    }

    public T get() {
        if (this.supplier != null) return this.supplier.get();
        return this.function.apply(this);
    }
}
