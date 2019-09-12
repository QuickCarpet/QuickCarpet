package quickcarpet.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.annotation.Feature;
import quickcarpet.settings.Settings;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Feature("explosionNoBlockDamage")
@Mixin(Explosion.class)
public abstract class ExplosionMixin {
    @Shadow @Final private List<BlockPos> affectedBlocks;
    
    @Redirect(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE",
            target = "Ljava/util/List;addAll(Ljava/util/Collection;)Z", remap = false))
    private boolean cancelAffectedBlocks(List list, Collection<?> c) {
        return false;
    }
    
    @Inject(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE",
            target = "Ljava/util/List;addAll(Ljava/util/Collection;)Z", remap = false), locals = LocalCapture.CAPTURE_FAILHARD)
    private void newAffectedBlocks(CallbackInfo ci, Set<BlockPos> set_1) {
        if (!Settings.explosionNoBlockDamage) {
            this.affectedBlocks.addAll(set_1);
        }
    }
}
