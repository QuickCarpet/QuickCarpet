package quickcarpet.pubsub;

public interface PubSubCallback {
    boolean shouldUpdate(int tickCounter);
    void update(PubSubNode node);
}
