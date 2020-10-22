package quickcarpet.mixin.profiler;

import net.minecraft.class_5459;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.PortalForcer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.api.annotation.Feature;
import quickcarpet.utils.CarpetProfiler;

import java.util.Optional;

@Feature("profiler")
@Mixin(PortalForcer.class)
public class PortalForcerMixin {
    @Shadow @Final private ServerWorld world;

    // getPortal
    @Inject(method = "method_30483", at = @At("HEAD"))
    private void startPortal(BlockPos blockPos, boolean bl, CallbackInfoReturnable<Optional<class_5459.class_5460>> cir) {
        CarpetProfiler.startSection(this.world, CarpetProfiler.SectionType.PORTALS);
    }

    @Inject(method = "method_30483", at = @At("RETURN"))
    private void endPortal(BlockPos blockPos, boolean bl, CallbackInfoReturnable<Optional<class_5459.class_5460>> cir) {
        CarpetProfiler.endSection(this.world);
    }

    // createPortal
    @Inject(method = "method_30482", at = @At("HEAD"))
    private void startPortal(BlockPos blockPos, Direction.Axis axis, CallbackInfoReturnable<Optional<class_5459.class_5460>> cir) {
        CarpetProfiler.startSection(this.world, CarpetProfiler.SectionType.PORTALS);
    }

    @Inject(method = "method_30482", at = @At("RETURN"))
    private void endPortal(BlockPos blockPos, Direction.Axis axis, CallbackInfoReturnable<Optional<class_5459.class_5460>> cir) {
        CarpetProfiler.endSection(this.world);
    }
}
