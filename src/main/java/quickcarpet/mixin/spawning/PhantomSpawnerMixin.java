package quickcarpet.mixin.spawning;

import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.gen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.annotation.Feature;
import quickcarpet.mixin.accessor.ServerChunkManagerAccessor;
import quickcarpet.settings.Settings;

@Feature("phantomsRespectMobcap")
@Mixin(PhantomSpawner.class)
public class PhantomSpawnerMixin {
    @Inject(method = "spawn", at = @At("HEAD"), cancellable = true)
    private void cancelSpawn(ServerWorld world, boolean spawnMonsters, boolean spawnAnimals, CallbackInfoReturnable<Integer> cir) {
        if (!spawnMonsters || !Settings.phantomsRespectMobcap) return;
        EntityCategory category = EntityType.PHANTOM.getCategory();
        int mobs = world.getMobCountsByCategory().getOrDefault(category, 0);
        int chunks = ((ServerChunkManagerAccessor) world.getChunkManager()).getTicketManager().getLevelCount();
        int max = chunks * category.getSpawnCap() / (17 * 17);
        if (mobs > max) {
            cir.setReturnValue(0);
            cir.cancel();
        }
    }
}
