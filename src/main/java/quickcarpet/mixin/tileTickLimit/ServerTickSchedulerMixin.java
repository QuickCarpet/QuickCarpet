package quickcarpet.mixin.tileTickLimit;

import net.minecraft.server.world.ServerTickScheduler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import quickcarpet.settings.Settings;

@Mixin(ServerTickScheduler.class)
public class ServerTickSchedulerMixin {
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 65536))
    private int tileTickLimit(int vanilla) {
        return Settings.tileTickLimit;
    }
}
