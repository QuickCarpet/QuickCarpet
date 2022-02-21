package quickcarpet.mixin.worldBorderSpawningFix;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.spawner.PatrolSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.settings.Settings;

import java.util.Random;

@Mixin(PatrolSpawner.class)
public class PatrolSpawnerMixin {
    @Inject(method = "spawnPillager", at = @At("HEAD"), cancellable = true)
    private void quickcarpet$worldBorderSpawningFix(ServerWorld world, BlockPos pos, Random random, boolean captain, CallbackInfoReturnable<Boolean> cir) {
        if (Settings.worldBorderSpawningFix && !world.getWorldBorder().contains(pos)) {
            cir.setReturnValue(false);
        }
    }
}
