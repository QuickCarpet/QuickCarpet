package quickcarpet.mixin.tileTickLimit;

import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.world.ScheduledTick;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.logging.LogParameter;
import quickcarpet.logging.Loggers;
import quickcarpet.settings.Settings;
import quickcarpet.utils.Messenger;

import java.util.Arrays;
import java.util.TreeSet;

@Mixin(ServerTickScheduler.class)
public class ServerTickSchedulerMixin {
    @Shadow @Final private TreeSet<ScheduledTick<?>> scheduledTickActionsInOrder;

    @Inject(method = "tick", at = @At(value = "CONSTANT", args = "intValue=65536", ordinal = 1))
    private void logTileTickLimit(CallbackInfo ci) {
        int scheduled = this.scheduledTickActionsInOrder.size();
        Loggers.TILE_TICK_LIMIT.log(() -> {
            return Messenger.t("logger.tileTickLimit.message", scheduled, Settings.tileTickLimit);
        }, () -> Arrays.asList(
            new LogParameter("NUMBER", scheduled),
            new LogParameter("LIMIT", Settings.tileTickLimit)
        ));
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 65536))
    private int tileTickLimit(int vanilla) {
        return Settings.tileTickLimit;
    }
}
