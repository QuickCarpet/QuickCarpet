package quickcarpet.mixin.fillLimit;

import net.minecraft.server.command.FillCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import quickcarpet.settings.Settings;

@Mixin(FillCommand.class)
public class FillCommandMixin {
    @ModifyConstant(method = "execute", constant = @Constant(intValue = 32768))
    private static int fillLimit(int old) {
        return Settings.fillLimit;
    }
}
