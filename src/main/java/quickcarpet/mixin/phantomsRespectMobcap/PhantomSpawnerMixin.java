package quickcarpet.mixin.phantomsRespectMobcap;

import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.gen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.helper.Mobcaps;
import quickcarpet.settings.Settings;

@Mixin(PhantomSpawner.class)
public class PhantomSpawnerMixin {
    @Inject(method = "spawn", at = @At("HEAD"), cancellable = true)
    private void cancelSpawn(ServerWorld world, boolean spawnMonsters, boolean spawnAnimals, CallbackInfoReturnable<Integer> cir) {
        if (!spawnMonsters || !Settings.phantomsRespectMobcap) return;
        if (!Mobcaps.isBelowCap(world.getChunkManager(), EntityType.PHANTOM.getSpawnGroup())) {
            cir.setReturnValue(0);
            cir.cancel();
        }
    }
}
