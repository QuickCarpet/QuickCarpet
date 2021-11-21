package quickcarpet.mixin.updateSuppressionCrashFix;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.settings.Settings;
import quickcarpet.utils.ThrowableUpdateSuppression;

@Mixin(World.class)
public class WorldMixin {
    @Inject(method = "updateNeighbor", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/crash/CrashReport;create(Ljava/lang/Throwable;Ljava/lang/String;)Lnet/minecraft/util/crash/CrashReport;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void checkUpdateSuppression(BlockPos pos, Block block, BlockPos fromPos, CallbackInfo ci, BlockState blockState, Throwable throwable) {
        if (Settings.updateSuppressionCrashFix) {
            if (throwable instanceof ThrowableUpdateSuppression e) throw e;
            if (throwable instanceof StackOverflowError e) {
                throw new ThrowableUpdateSuppression("Update suppression", e);
            }
        }
    }
}
