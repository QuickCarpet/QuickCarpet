package quickcarpet.utils.mixin.extensions;

import java.util.concurrent.atomic.AtomicInteger;

public interface ExtendedServerLightingProvider {
    class Data {
        public final AtomicInteger enqueued = new AtomicInteger();
        public final AtomicInteger executed = new AtomicInteger();
        public final AtomicInteger queueSize = new AtomicInteger();
        public float enqueuedPerTick;
        public float executedPerTick;
        public int batchSize;
    }

    Data getData();
    void tickData();
}
