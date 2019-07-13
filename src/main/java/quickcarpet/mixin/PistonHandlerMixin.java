package quickcarpet.mixin;

import net.minecraft.block.piston.PistonHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import quickcarpet.annotation.Feature;
import quickcarpet.settings.Settings;

@Feature("pushLimit")
@Mixin(PistonHandler.class)
public class PistonHandlerMixin {
    @ModifyConstant(method = "tryMove", constant = @Constant(intValue = 12), expect = 3)
    private int adjustPushLimit(int pushLimit) {
        return Settings.pushLimit;
    }
}
