package quickcarpet.logging.loghelpers;

import quickcarpet.logging.Logger;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class PacketCounter {
    public static long totalOut = 0;
    public static long totalIn = 0;

    public static void reset() {
        totalIn = 0;
        totalOut = 0;
    }

    public static class LogCommandParameters extends AbstractMap<String, Object> implements Logger.CommandParameters {
        public static final LogCommandParameters INSTANCE = new LogCommandParameters();
        private LogCommandParameters() {}
        @Override
        public Set<Entry<String, Object>> entrySet() {
            Map<String, Object> counts = new LinkedHashMap<>();
            counts.put("TOTAL_IN", totalIn);
            counts.put("TOTAL_OUT", totalOut);
            return counts.entrySet();
        }
    }
}
