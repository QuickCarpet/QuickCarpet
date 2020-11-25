package quickcarpet.logging.loghelpers;

import net.minecraft.entity.TntEntity;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import quickcarpet.logging.Loggers;

import java.util.Arrays;

import static quickcarpet.utils.Messenger.*;

public class TNTLogHelper {
    private final TntEntity tnt;
    private final Vec3d primedPos;
    private final double primedAngle;

    /**
     * Runs when the TNT is primed. Expects the position and motion angle of the TNT.
     */
    public TNTLogHelper(TntEntity tnt) {
        this.tnt = tnt;
        this.primedPos = tnt.getPos();
        Vec3d v = tnt.getVelocity();
        if (v.x == 0 && v.z == 0) {
            primedAngle = Double.NaN;
        } else {
            double angle = -Math.toDegrees(Math.atan2(v.x, v.z));
            if (angle < 0) angle += 360;
            primedAngle = angle == -0 ? 0 : angle;
        }
    }

    /**
     * Runs when the TNT explodes. Expects the position of the TNT.
     */
    public void onExploded() {
        Vec3d primed = this.primedPos;
        Vec3d exploded = tnt.getPos();
        Loggers.TNT.log(option -> {
            switch (option) {
                case "brief":
                    return new MutableText[]{c(
                        style(c(s("P "), dblt(primed.x, primed.y, primed.z, primedAngle)), Formatting.GREEN),
                        style(c(s("E "), dblt(exploded.x, exploded.y, exploded.z)), Formatting.RED)
                    )};
                case "full":
                    return new MutableText[]{c(
                        style(c(s("P "), dblf(primed.x, primed.y, primed.z, primedAngle)), Formatting.GREEN),
                        style(c(s("E "), dblf(exploded.x, exploded.y, exploded.z)), Formatting.RED)
                    )};
            }
            return null;
        }, () -> Arrays.asList(
            new LogParameter("primed.x", primed.x),
            new LogParameter("primed.x", primed.y),
            new LogParameter("primed.x", primed.z),
            new LogParameter("primed.angle", primedAngle),
            new LogParameter("exploded.x", exploded.x),
            new LogParameter("exploded.y", exploded.y),
            new LogParameter("exploded.z", exploded.z)
        ));
    }
}
