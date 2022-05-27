package quickcarpet.logging.source;

import quickcarpet.logging.LogParameter;
import quickcarpet.logging.Logger;

import java.util.List;

import static quickcarpet.utils.Messenger.s;

public class PacketCounterLoggerSource implements LoggerSource {
    private static PacketCounterLoggerSource instance;
    private long totalOut = 0;
    private long totalIn = 0;
    private long previousOut = 0;
    private long previousIn = 0;

    public PacketCounterLoggerSource() {
        instance = this;
    }

    public void reset() {
        previousIn = totalIn;
        previousOut = totalOut;
        totalIn = 0;
        totalOut = 0;
    }

    public static void in() {
        if (instance == null) return;
        instance.totalIn++;
    }

    public static void out() {
        if (instance == null) return;
        instance.totalOut++;
    }

    @Override
    public void pull(Logger logger) {
        PacketCounterLoggerSource source = instance;
        if (source == null) return;
        source.reset();
        logger.log(() -> s("I/" + source.previousIn + " O/" + source.previousOut), () -> List.of(
            new LogParameter("in", source.previousIn),
            new LogParameter("out", source.previousOut)
        ));
    }
}
