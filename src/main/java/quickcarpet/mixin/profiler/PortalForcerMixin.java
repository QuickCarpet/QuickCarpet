package quickcarpet.mixin.profiler;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.PortalForcer;
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

    @Inject(method = "getPortalRect", at = @At("HEAD"))
    private void startPortal(BlockPos blockPos, boolean bl, CallbackInfoReturnable<Optional<BlockLocating.Rectangle>> cir) {
        CarpetProfiler.startSection(this.world, CarpetProfiler.SectionType.PORTALS);
    }

    @Inject(method = "getPortalRect", at = @At("RETURN"))
    private void endPortal(BlockPos blockPos, boolean bl, CallbackInfoReturnable<Optional<BlockLocating.Rectangle>> cir) {
        CarpetProfiler.endSection(this.world, CarpetProfiler.SectionType.PORTALS);
    }

    @Inject(method = "createPortal", at = @At("HEAD"))
    private void startPortal(BlockPos blockPos, Direction.Axis axis, CallbackInfoReturnable<Optional<BlockLocating.Rectangle>> cir) {
        CarpetProfiler.startSection(this.world, CarpetProfiler.SectionType.PORTALS);
    }

    @Inject(method = "createPortal", at = @At("RETURN"))
    private void endPortal(BlockPos blockPos, Direction.Axis axis, CallbackInfoReturnable<Optional<BlockLocating.Rectangle>> cir) {
        CarpetProfiler.endSection(this.world, CarpetProfiler.SectionType.PORTALS);
    }
}
