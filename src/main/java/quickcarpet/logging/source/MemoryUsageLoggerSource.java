package quickcarpet.logging.source;

import net.minecraft.util.Formatting;
import quickcarpet.logging.LogParameter;
import quickcarpet.logging.Logger;

import java.util.List;

import static quickcarpet.utils.Messenger.*;

public class MemoryUsageLoggerSource implements LoggerSource {
    @Override
    public void pull(Logger logger) {
        var runtime = Runtime.getRuntime();
        long max = runtime.maxMemory();
        long allocated = runtime.totalMemory();
        long used = allocated - runtime.freeMemory();
        logger.log(() -> c(
            s(Long.toString(used / (1024 * 1024)), getHeatmapColor(used, max)), s("MB", Formatting.GRAY),
            s("/"),
            s(Long.toString(max / (1024 * 1024)), Formatting.AQUA), s("MB", Formatting.GRAY)
        ), () -> List.of(
            new LogParameter("used", used),
            new LogParameter("free", runtime.freeMemory()),
            new LogParameter("max", max),
            new LogParameter("total", runtime.totalMemory())
        ));
    }
}
