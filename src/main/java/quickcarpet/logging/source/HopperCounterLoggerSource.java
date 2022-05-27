package quickcarpet.logging.source;

import net.minecraft.text.MutableText;
import quickcarpet.QuickCarpetServer;
import quickcarpet.helper.HopperCounter;
import quickcarpet.logging.Logger;

import java.util.Collections;
import java.util.List;

import static quickcarpet.utils.Messenger.c;

public class HopperCounterLoggerSource implements LoggerSource {
    @Override
    public void pull(Logger logger) {
        logger.log(key -> {
            HopperCounter counter = HopperCounter.getCounter(key);
            List<MutableText> res = counter == null ? Collections.emptyList() : counter.format(QuickCarpetServer.getMinecraftServer(), false, true);
            return c(res.toArray(new MutableText[0]));
        }, () -> HopperCounter.COMMAND_PARAMETERS);
    }
}
