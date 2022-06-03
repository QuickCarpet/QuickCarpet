package quickcarpet.logging.source;

import quickcarpet.logging.Logger;

import java.util.Arrays;
import java.util.List;

public interface LoggerSource {
    default List<String> parseOptions(String option) {
        return List.of(option);
    }
    default void tick() {}
    void pull(Logger logger);

    static List<String> splitOptions(String option) {
        return Arrays.asList(option.split("[,+]"));
    }
}
