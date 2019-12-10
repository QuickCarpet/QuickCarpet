package quickcarpet.mixin.profiler;

import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PortalForcer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.annotation.Feature;
import quickcarpet.utils.CarpetProfiler;

@Feature("profiler")
@Mixin(PortalForcer.class)
public class PortalForcerMixin {
    @Shadow @Final private ServerWorld world;

    @Inject(method = "getPortal", at = @At("HEAD"))
    private void startPortal(BlockPos pos, Vec3d velocity, Direction direction, double yaw, double pitch, boolean isPlayer, CallbackInfoReturnable<BlockPattern.TeleportTarget> cir) {
        CarpetProfiler.startSection(this.world, CarpetProfiler.SectionType.PORTALS);
    }

    @Inject(method = "getPortal", at = @At("RETURN"))
    private void endPortal(BlockPos pos, Vec3d velocity, Direction direction, double yaw, double pitch, boolean isPlayer, CallbackInfoReturnable<BlockPattern.TeleportTarget> cir) {
        CarpetProfiler.endSection(this.world);
    }

    @Inject(method = "createPortal", at = @At("HEAD"))
    private void startPortal(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        CarpetProfiler.startSection(this.world, CarpetProfiler.SectionType.PORTALS);
    }

    @Inject(method = "createPortal", at = @At("RETURN"))
    private void endPortal(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        CarpetProfiler.endSection(this.world);
    }
}
