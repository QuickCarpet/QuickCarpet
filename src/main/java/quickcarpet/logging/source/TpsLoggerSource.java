package quickcarpet.logging.source;

import net.minecraft.util.Formatting;
import quickcarpet.feature.TickSpeed;
import quickcarpet.logging.LogParameter;
import quickcarpet.logging.Logger;

import java.util.Arrays;

import static quickcarpet.utils.Messenger.*;

public class TpsLoggerSource implements LoggerSource {
    private final TickSpeed tickSpeed = TickSpeed.getServerTickSpeed();

    @Override
    public void pull(Logger logger) {
        double MSPT = tickSpeed.getCurrentMSPT();
        double TPS = tickSpeed.calculateTPS(MSPT);
        Formatting color = getHeatmapColor(MSPT, tickSpeed.msptGoal);
        logger.log(() -> c(
            s("TPS: ", Formatting.GRAY), formats("%.1f", color, TPS),
            s(" MSPT: ", Formatting.GRAY), formats("%.1f", color, MSPT)
        ), () -> Arrays.asList(
            new LogParameter("MSPT", tickSpeed::getCurrentMSPT),
            new LogParameter("TPS", tickSpeed::getTPS)
        ));
    }
}
