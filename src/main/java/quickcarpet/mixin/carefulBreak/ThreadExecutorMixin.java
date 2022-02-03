package quickcarpet.mixin.carefulBreak;

import net.minecraft.util.thread.ThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.utils.CarefulBreakHelper;

@Mixin(ThreadExecutor.class)
public class ThreadExecutorMixin {
    @Inject(method = "executeTask", at = @At("RETURN"))
    private void quickcarpet$carefulBreak$afterTask(Runnable task, CallbackInfo ci) {
        CarefulBreakHelper.miningPlayer.set(null);
    }
}
