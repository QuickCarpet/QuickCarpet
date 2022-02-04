package quickcarpet.mixin.pushLimit;

import net.minecraft.block.piston.PistonHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import quickcarpet.settings.Settings;

@Mixin(PistonHandler.class)
public class PistonHandlerMixin {
    @ModifyConstant(method = "tryMove", constant = @Constant(intValue = 12), expect = 3)
    private int quickcarpet$pushLimit(int pushLimit) {
        return Settings.pushLimit;
    }
}
