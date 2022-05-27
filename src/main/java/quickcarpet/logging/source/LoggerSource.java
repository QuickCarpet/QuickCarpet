package quickcarpet.logging.source;

import quickcarpet.logging.Logger;

public interface LoggerSource {
    void pull(Logger logger);
}
