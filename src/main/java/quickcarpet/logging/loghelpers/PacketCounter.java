package quickcarpet.logging.loghelpers;

import quickcarpet.logging.Logger;

import java.util.AbstractMap;
import java.util.Set;

public class PacketCounter {
    public static long totalOut = 0;
    public static long totalIn = 0;
    private static long previousOut = 0;
    private static long previousIn = 0;

    public static void reset() {
        previousIn = totalIn;
        previousOut = totalOut;
        totalIn = 0;
        totalOut = 0;
    }

    public static class LogCommandParameters extends AbstractMap<String, Long> implements Logger.CommandParameters<Long> {
        public static final LogCommandParameters INSTANCE = new LogCommandParameters();
        private LogCommandParameters() {}
        @Override
        public Set<Entry<String, Long>> entrySet() {
            return LogParameter.parameters(
                    new LogParameter<>("in", () -> previousIn),
                    new LogParameter<>("out", () -> previousOut));
        }
    }
}
