package quickcarpet.logging.source;

import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import quickcarpet.helper.TickSpeed;
import quickcarpet.logging.LogParameter;
import quickcarpet.logging.Logger;

import java.util.List;

import static quickcarpet.utils.Messenger.*;

public class TickWarpLoggerSource implements LoggerSource {
    private static final Identifier UNIFORM_FONT = new Identifier("uniform");
    // " ", "▏", "▎", "▍", "▌", "▋", "▊", "▉", "█" would be best, but Minecraft's font isn't monospaced
    private static final String[] PROGRESS_CHARS = new String[]{"---", "▍--", "▍▍-", "▍▍▍"};

    @Override
    public void pull(Logger logger) {
        TickSpeed tickSpeed = TickSpeed.getServerTickSpeed();
        long total = tickSpeed.getWarpTimeTotal();
        long remaining = tickSpeed.getWarpTimeRemaining();
        if (total == 0) return;
        double progress = MathHelper.clamp(1 - (double) remaining / total, 0, 1);
        logger.log((option) -> switch (option) {
            case "value" -> renderDescription(total, remaining, progress);
            case "bar" -> renderBar(progress);
            default -> c(
                renderBar(progress),
                s(" "),
                renderDescription(total, remaining, progress)
            );
        }, () -> List.of(
            new LogParameter("completed", total - remaining),
            new LogParameter("remaining", remaining),
            new LogParameter("total", total),
            new LogParameter("progress", Math.round(progress * 100))
        ));
    }

    private static MutableText renderDescription(long total, long remaining, double progress) {
        return format("%d/%d (%.1f%%)", total - remaining, total, progress * 100);
    }

    private static MutableText renderBar(double progress) {
        int progressBarWidth = 10;
        double barProgress = progress * progressBarWidth;
        int completeBarUnits = (int) barProgress;
        int partialUnit = (int)((barProgress - completeBarUnits) * (PROGRESS_CHARS.length - 1));
        StringBuilder bar = new StringBuilder();
        bar.append(PROGRESS_CHARS[PROGRESS_CHARS.length - 1].repeat(completeBarUnits));
        if (completeBarUnits < progressBarWidth) {
            bar.append(PROGRESS_CHARS[partialUnit]);
            bar.append(PROGRESS_CHARS[0].repeat(progressBarWidth - completeBarUnits - 1));
        }
        return s(bar.toString(), Formatting.STRIKETHROUGH).styled(s -> s.withFont(UNIFORM_FONT));
    }
}
