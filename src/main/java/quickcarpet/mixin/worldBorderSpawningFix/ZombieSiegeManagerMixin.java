package quickcarpet.mixin.worldBorderSpawningFix;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.ZombieSiegeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.settings.Settings;

@Mixin(ZombieSiegeManager.class)
public class ZombieSiegeManagerMixin {
    @Shadow private int startX;
    @Shadow private int startY;
    @Shadow private int startZ;

    @Inject(method = "trySpawnZombie", at = @At("HEAD"), cancellable = true)
    private void quickcarpet$worldBorderSpawningFix(ServerWorld world, CallbackInfo ci) {
        if (Settings.worldBorderSpawningFix && !world.getWorldBorder().contains(new BlockPos(startX, startY, startZ))) {
            ci.cancel();
        }
    }
}
