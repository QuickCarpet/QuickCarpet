package quickcarpet.pubsub;

import javax.annotation.Nullable;
import java.util.*;

/**
 * A node in the PubSub tree
 * Classes outside this package should interact with {@link PubSubManager} passing nodes as arguments
 */
public final class PubSubNode {
    public final String name;
    public final String fullName;

    public final @Nullable PubSubNode parent;
    public final Map<String, PubSubNode> children = new HashMap<>();
    private Set<PubSubSubscriber> subscribers = new LinkedHashSet<>();
    private Set<PubSubCallback> callbacks = new LinkedHashSet<>();

    /**
        Keeps track of the number of subscribers in this branch of the tree (root -> subscriber)
        Used for only updating values if there are actually subscribers that would get them
     */
    private int totalSubscriberCount = 0;
    private Object lastValue;

    PubSubNode(@Nullable PubSubNode parent, String name) {
        this.parent = parent;
        this.name = name;
        this.fullName = parent != null && parent.fullName.length() > 0 ? parent.fullName + "." + name : name;
    }

    @Nullable
    public PubSubNode getChildNode(String ...path) {
        return getChildNode(path, 0, false);
    }

    @Nullable
    PubSubNode getChildNode(Collection<String> path) {
        return getChildNode(path.toArray(new String[0]), 0, false);
    }

    public PubSubNode getOrCreateChildNode(String ...path) {
        return getChildNode(path, 0, true);
    }

    PubSubNode getOrCreateChildNode(Collection<String> path) {
        return getChildNode(path.toArray(new String[0]), 0, true);
    }

    private PubSubNode getChildNode(String[] path, int offset, boolean create) {
        if (path.length == offset) return this;
        String childName = path[offset];
        PubSubNode child = children.get(childName);
        if (child == null) {
            if (!create) return null;
            child = new PubSubNode(this, childName);
            children.put(childName, child);
        }
        return child.getChildNode(path, offset + 1, create);
    }


    /**
     * Add a subscriber to this node
     * Updates {@link #totalSubscriberCount} for all parents and children
     * @param subscriber The subscriber being added
     */
    void subscribe(PubSubSubscriber subscriber) {
        this.subscribers.add(subscriber);
        for (PubSubNode n = this.parent; n != null; n = n.parent) {
            n.totalSubscriberCount++;
        }
        this.onSubscribe(subscriber);
    }

    /**
     * Updates children after subscribing.
     * Called recursively and initially by {@link #subscribe(PubSubSubscriber)}
     * @param subscriber The subscriber being added
     */
    private void onSubscribe(PubSubSubscriber subscriber) {
        this.totalSubscriberCount++;
        if (lastValue != null) subscriber.updateValue(this, lastValue);
        for (PubSubNode child : children.values()) {
            child.onSubscribe(subscriber);
        }
    }

    /**
     * Remove a subscriber from this node
     * Updates {@link #totalSubscriberCount} for all parents and children
     * @param subscriber The subscriber being removed
     */
    void unsubscribe(PubSubSubscriber subscriber) {
        this.subscribers.remove(subscriber);
        for (PubSubNode n = this.parent; n != null; n = n.parent) {
            n.totalSubscriberCount--;
        }
        this.onUnsubscribe(subscriber);
    }

    /**
     * Updates children after unsubscribing.
     * Called recursively and initially by {@link #unsubscribe(PubSubSubscriber)}
     * @param subscriber The subscriber being removed
     */
    private void onUnsubscribe(PubSubSubscriber subscriber) {
        this.totalSubscriberCount--;
        for (PubSubNode child : children.values()) {
            child.onUnsubscribe(subscriber);
        }
    }

    /**
     * Publish new value
     * @param value New value for this node
     * @see #publish(PubSubNode,Object)
     */
    void publish(Object value) {
        lastValue = value;
        publish(this, value);
    }

    /**
     * Publish value and propagate it up the tree
     * @param node Node being updated
     * @param value New value of the node
     */
    private void publish(PubSubNode node, Object value) {
        for (PubSubSubscriber subscriber : subscribers) {
            subscriber.updateValue(node, value);
        }
        if (parent != null) parent.publish(node, value);
    }

    /**
     * Instruct this node to update values from providers (called every tick).
     * May already be suppressed by the parent if there are no subscribers.
     * @param tickCounter Tick counter of the minecraft server
     */
    void update(int tickCounter) {
        if (totalSubscriberCount == 0) return;
        for (PubSubCallback cb : this.callbacks) {
            if (cb.shouldUpdate(tickCounter)) cb.update(this);
        }
        for (PubSubNode child : children.values()) {
            child.update(tickCounter);
        }
    }

    PubSubManager.CallbackHandle addCallback(PubSubCallback callback) {
        callbacks.add(callback);
        return new PubSubManager.CallbackHandle() {
            @Override
            public boolean isActive() {
                return callbacks.contains(callback);
            }

            @Override
            public void remove() {
                callbacks.remove(callback);
            }
        };
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.parent) * 31 + this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == PubSubNode.class
                && Objects.equals(((PubSubNode) obj).parent, this.parent)
                && this.name.equals(((PubSubNode) obj).name);
    }

    @Override
    public String toString() {
        return "PubSubNode[" + this.fullName + ",subs=" + totalSubscriberCount + "]";
    }
}
