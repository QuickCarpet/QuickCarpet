package quickcarpet.mixin.tileTickLimit.compat.lithium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import quickcarpet.settings.Settings;

@Pseudo
@Mixin(targets = "me.jellysquid.mods.lithium.common.world.scheduler.LithiumServerTickScheduler", remap = false)
public class LithiumServerTickSchedulerMixin {
    @ModifyConstant(method = "selectTicks(Lnet/minecraft/server/world/ServerChunkManager;J)V", remap = false, constant = @Constant(intValue = 65536))
    private int tileTickLimit(int vanilla) {
        return Settings.tileTickLimit;
    }
}