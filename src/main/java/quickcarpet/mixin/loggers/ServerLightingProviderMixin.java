package quickcarpet.mixin.loggers;

import net.minecraft.server.world.ServerLightingProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.utils.mixin.extensions.ExtendedServerLightingProvider;

@Mixin(ServerLightingProvider.class)
public class ServerLightingProviderMixin implements ExtendedServerLightingProvider {
    private static final float EMA_FACTOR = 0.9f;

    @Shadow private volatile int taskBatchSize;
    private final Data data = new Data();

    @Override
    public Data getData() {
        data.batchSize = taskBatchSize;
        return data;
    }

    @Override
    public void tickData() {
        data.enqueuedPerTick = smooth(data.enqueuedPerTick, data.enqueued.getAndSet(0));
        data.executedPerTick = smooth(data.executedPerTick, data.executed.getAndSet(0));
    }

    @Unique
    private static float smooth(float previous, float current) {
        if (previous < 1e-7) return current;
        return EMA_FACTOR * previous + (1 - EMA_FACTOR) * current;
    }

    @Inject(method = "enqueue(IILjava/util/function/IntSupplier;Lnet/minecraft/server/world/ServerLightingProvider$Stage;Ljava/lang/Runnable;)V", at = @At("TAIL"))
    private void quickcarpet$log$lightQueue$onEnqueue(CallbackInfo ci) {
        data.enqueued.incrementAndGet();
        data.queueSize.incrementAndGet();
    }

    @Inject(method = "runTasks", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/ObjectListIterator;remove()V", remap = false))
    private void quickcarpet$log$lightQueue$onExecute(CallbackInfo ci) {
        data.executed.incrementAndGet();
        data.queueSize.decrementAndGet();
    }
}
