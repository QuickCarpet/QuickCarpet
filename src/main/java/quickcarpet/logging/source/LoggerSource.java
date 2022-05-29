package quickcarpet.logging.source;

import quickcarpet.logging.Logger;

public interface LoggerSource {
    default void tick() {}
    void pull(Logger logger);
}
