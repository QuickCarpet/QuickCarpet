package quickcarpet.pubsub;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 * Central interface for PubSub interactions
 * @see quickcarpet.QuickCarpet#PUBSUB
 */
public final class PubSubManager {
    public final PubSubNode ROOT = new PubSubNode(null, "");
    private final Map<String, PubSubNode> knownNodes = new TreeMap<>();

    /**
     * Get a node if it already exists.
     * Call this for client-initiated requests
     * @param name The path to the node (separated by ".")
     * @return The node requested or null if it does not exist
     * @see PubSubNode#getChildNode(String...)
     * @see PubSubNode#getChildNode(Collection)
     */
    @Nullable
    public PubSubNode getNode(String name) {
        return knownNodes.get(name);
    }

    /**
     * Get a node or create one if it does not exist
     * Call this for server-initiated requests (when publishing)
     * @param name The path to the node (separated by ".")
     * @return The node requested
     * @see PubSubNode#getOrCreateChildNode(String...)
     * @see PubSubNode#getOrCreateChildNode(Collection)
     */
    public PubSubNode getOrCreateNode(String name) {
        return knownNodes.computeIfAbsent(name, name1 -> {
            String[] path = name1.split("\\.");
            return addKnownNode(ROOT.getOrCreateChildNode(path));
        });
    }

    public PubSubNode addKnownNode(PubSubNode node) {
        for (PubSubNode n = node; n != ROOT; n = n.parent) {
            knownNodes.put(n.fullName, n);
        }
        return node;
    }

    public void subscribe(PubSubNode node, PubSubSubscriber subscriber) {
        node.subscribe(subscriber);
    }

    public void unsubscribe(PubSubNode node, PubSubSubscriber subscriber) {
        node.unsubscribe(subscriber);
    }

    public void publish(PubSubNode node, Object value) {
        node.publish(value);
    }

    public void update(int tickCounter) {
        ROOT.update(tickCounter);
    }

    public CallbackHandle addCallback(PubSubNode node, int interval, Consumer<PubSubNode> cb) {
        return node.addCallback(new PubSubCallback() {
            @Override
            public boolean shouldUpdate(int tickCounter) {
                return (tickCounter % interval) == 0;
            }

            @Override
            public void update(PubSubNode node) {
                cb.accept(node);
            }
        });
    }

    public CallbackHandle addCallback(PubSubNode node, PubSubCallback cb) {
        return node.addCallback(cb);
    }

    public interface CallbackHandle {
        boolean isActive();
        void remove();
    }
}
