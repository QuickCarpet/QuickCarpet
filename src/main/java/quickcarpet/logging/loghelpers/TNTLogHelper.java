package quickcarpet.logging.loghelpers;

import net.minecraft.entity.TntEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import quickcarpet.logging.Logger;
import quickcarpet.logging.Loggers;

import java.util.LinkedHashMap;

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
                    return new Text[]{c(
                            "l P ", style(dblt(primed.x, primed.y, primed.z, primedAngle), LIME),
                            "r  E ", style(dblt(exploded.x, exploded.y, exploded.z), RED))};
                case "full":
                    return new Text[]{c(
                            "l P ", style(dblf(primed.x, primed.y, primed.z, primedAngle), LIME),
                            "r  E ", style(dblf(exploded.x, exploded.y, exploded.z), RED))};
            }
            return null;
        }, () -> new LogParameters(primed, primedAngle, exploded));
    }

    public static class LogParameters extends LinkedHashMap<String, Double> implements Logger.CommandParameters<Double> {
        public LogParameters(Vec3d primed, double angle, Vec3d exploded) {
            put("primed.x", primed.x);
            put("primed.y", primed.y);
            put("primed.z", primed.z);
            put("primed.angle", angle);
            put("exploded.x", exploded.x);
            put("exploded.y", exploded.y);
            put("exploded.z", exploded.z);
        }
    }
}
