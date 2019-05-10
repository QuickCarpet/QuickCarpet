package quickcarpet.logging.loghelpers;

import net.minecraft.network.chat.Component;
import quickcarpet.logging.LoggerRegistry;
import quickcarpet.utils.Messenger;

public class TNTLogHelper
{
    private double primedX, primedY, primedZ, primedAngle;
    /**
     * Runs when the TNT is primed. Expects the position and motion angle of the TNT.
     */
    public void onPrimed(double x, double y, double z, double angle)
    {
        primedX = x;
        primedY = y;
        primedZ = z;
        primedAngle = angle;
    }
    /**
     * Runs when the TNT explodes. Expects the position of the TNT.
     */
    public void onExploded(double x, double y, double z)
    {
        LoggerRegistry.getLogger("tnt").log( (option) -> {
            switch (option)
            {
                case "brief":
                    return new Component[]{Messenger.c(
                            "l P ",Messenger.dblt("l",primedX,primedY,primedZ,primedAngle),
                            "r  E ",Messenger.dblt("r",x, y, z))};
                case "full":
                    return new Component[]{Messenger.c(
                            "l P ",Messenger.dblf("l",primedX,primedY,primedZ,primedAngle),
                            "r  E ",Messenger.dblf("r",x, y, z))};
            }
            return null;
        });
    }

}
