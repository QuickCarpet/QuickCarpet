package quickcarpet.logging.loghelpers;

import net.minecraft.entity.TntEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import quickcarpet.logging.Logger;
import quickcarpet.logging.LoggerRegistry;
import quickcarpet.utils.Messenger;

import java.util.LinkedHashMap;

public class TNTLogHelper {
    private final TntEntity tnt;
    private final double primedX, primedY, primedZ, primedAngle;

    /**
     * Runs when the TNT is primed. Expects the position and motion angle of the TNT.
     */
    public TNTLogHelper(TntEntity tnt) {
        this.tnt = tnt;
        primedX = tnt.x;
        primedY = tnt.y;
        primedZ = tnt.z;
        Vec3d v = tnt.getVelocity();
        primedAngle = -Math.toDegrees(Math.atan2(v.x, v.z));
    }

    /**
     * Runs when the TNT explodes. Expects the position of the TNT.
     */
    public void onExploded() {
        double x = tnt.x;
        double y = tnt.y;
        double z = tnt.z;
        LoggerRegistry.TNT.log(option -> {
            switch (option) {
                case "brief":
                    return new Text[]{Messenger.c(
                            "l P ", Messenger.dblt("l", primedX, primedY, primedZ, primedAngle),
                            "r  E ", Messenger.dblt("r", x, y, z))};
                case "full":
                    return new Text[]{Messenger.c(
                            "l P ", Messenger.dblf("l", primedX, primedY, primedZ, primedAngle),
                            "r  E ", Messenger.dblf("r", x, y, z))};
            }
            return null;
        }, () -> new LogParameters(x, y, z));
    }

    public class LogParameters extends LinkedHashMap<String, Double> implements Logger.CommandParameters<Double> {
        public LogParameters(double x, double y, double z) {
            put("primed.x", primedX);
            put("primed.y", primedY);
            put("primed.z", primedZ);
            put("primed.angle", primedAngle);
            put("exploded.x", x);
            put("exploded.y", y);
            put("exploded.z", z);
        }
    }
}
