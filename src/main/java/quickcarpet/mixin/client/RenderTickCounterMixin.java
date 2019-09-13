package quickcarpet.mixin.client;

import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.QuickCarpet;
import quickcarpet.annotation.Feature;
import quickcarpet.client.ClientSetting;

@Feature("tickSpeed")
@Mixin(RenderTickCounter.class)
public class RenderTickCounterMixin {
    @Shadow  @Final private float tickTime;

    @Redirect(method = "beginRenderTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderTickCounter;tickTime:F"))
    private float adjustTickSpeed(RenderTickCounter counter) {
        float defaultGoal = tickTime;
        float goal = QuickCarpet.getInstance().client.tickSpeed.msptGoal;
        if ((goal > defaultGoal && ClientSetting.SYNC_LOW_TPS.get()) || (goal < defaultGoal && ClientSetting.SYNC_HIGH_TPS.get())) {
            return goal;
        }
        return defaultGoal;
    }
}
