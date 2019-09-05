package quickcarpet.mixin.client;

import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.QuickCarpet;
import quickcarpet.annotation.Feature;

@Feature("tickSpeed")
@Mixin(RenderTickCounter.class)
public class RenderTickCounterMixin {
    @Redirect(method = "beginRenderTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderTickCounter;timeScale:F"))
    private float adjustTickSpeed(RenderTickCounter counter) {
        return QuickCarpet.getInstance().client.tickSpeed.msptGoal;
    }
}
