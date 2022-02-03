package quickcarpet.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.QuickCarpetClient;
import quickcarpet.client.ClientSetting;

@Mixin(RenderTickCounter.class)
@Environment(EnvType.CLIENT)
public class RenderTickCounterMixin {
    @Shadow  @Final private float tickTime;

    @Redirect(method = "beginRenderTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderTickCounter;tickTime:F"))
    private float quickcarpet$adjustTickSpeed(RenderTickCounter counter) {
        float defaultGoal = tickTime;
        float goal = QuickCarpetClient.getInstance().tickSpeed.msptGoal;
        if ((goal > defaultGoal && ClientSetting.SYNC_LOW_TPS.get()) || (goal < defaultGoal && ClientSetting.SYNC_HIGH_TPS.get())) {
            return goal;
        }
        return defaultGoal;
    }
}
