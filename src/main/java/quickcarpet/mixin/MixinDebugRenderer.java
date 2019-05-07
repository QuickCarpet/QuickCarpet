package quickcarpet.mixin;

import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.render.debug.GoalSelectorDebugRenderer;
import net.minecraft.client.render.debug.PointOfInterestDebugRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DebugRenderer.class)
public class MixinDebugRenderer {
    @Shadow @Final public DebugRenderer.Renderer chunkLoadingDebugRenderer;

    @Shadow @Final public PointOfInterestDebugRenderer pointsOfInterestDebugRenderer;

    @Shadow @Final public GoalSelectorDebugRenderer goalSelectorDebugRenderer;

    @Shadow @Final public DebugRenderer.Renderer heightmapDebugRenderer;

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void alwaysRender(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
        cir.cancel();
    }

    @Inject(method = "renderDebuggers", at = @At("TAIL"))
    private void renderMoreDebuggers(long long_1, CallbackInfo ci) {
        // this.chunkLoadingDebugRenderer.render(long_1);
        // this.pointsOfInterestDebugRenderer.render(long_1);
        // this.goalSelectorDebugRenderer.render(long_1);
        // this.heightmapDebugRenderer.render(long_1);
    }
}
