package quickcarpet.mixin;

import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.gen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.settings.Settings;

@Mixin(PhantomSpawner.class)
public class PhantomSpawnerMixin {
    @Inject(method = "spawn", at = @At("HEAD"), cancellable = true)
    private void cancelSpawn(ServerWorld world, boolean spawnMonsters, boolean spawnAnimals, CallbackInfoReturnable<Integer> cir) {
        if (!spawnMonsters || !Settings.phantomsRespectMobcap) return;
        EntityCategory category = EntityType.PHANTOM.getCategory();
        int mobs = world.getMobCountsByCategory().getOrDefault(category, 0);
        int chunks = ((ServerChunkManagerAccessor) world.method_14178()).getTicketManager().getLevelCount();
        int max = chunks * category.getSpawnCap() / (17 * 17);
        if (mobs > max) {
            cir.setReturnValue(0);
            cir.cancel();
        }
    }
}
