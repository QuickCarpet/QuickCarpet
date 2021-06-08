package quickcarpet.mixin.profiler;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.PortalForcer;
import net.minecraft.world.PortalUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.utils.CarpetProfiler;

import java.util.Optional;

@Mixin(PortalForcer.class)
public class PortalForcerMixin {
    @Shadow @Final private ServerWorld world;

    // getPortal
    @Inject(method = "method_30483", at = @At("HEAD"))
    private void startPortal(BlockPos blockPos, boolean bl, CallbackInfoReturnable<Optional<PortalUtil.Rectangle>> cir) {
        CarpetProfiler.startSection(this.world, CarpetProfiler.SectionType.PORTALS);
    }

    @Inject(method = "method_30483", at = @At("RETURN"))
    private void endPortal(BlockPos blockPos, boolean bl, CallbackInfoReturnable<Optional<PortalUtil.Rectangle>> cir) {
        CarpetProfiler.endSection(this.world);
    }

    // createPortal
    @Inject(method = "method_30482", at = @At("HEAD"))
    private void startPortal(BlockPos blockPos, Direction.Axis axis, CallbackInfoReturnable<Optional<PortalUtil.Rectangle>> cir) {
        CarpetProfiler.startSection(this.world, CarpetProfiler.SectionType.PORTALS);
    }

    @Inject(method = "method_30482", at = @At("RETURN"))
    private void endPortal(BlockPos blockPos, Direction.Axis axis, CallbackInfoReturnable<Optional<PortalUtil.Rectangle>> cir) {
        CarpetProfiler.endSection(this.world);
    }
}
