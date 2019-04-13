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
import quickcarpet.QuickCarpetSettings;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Mixin(Explosion.class)
public abstract class MixinExplosion
{
    @Shadow @Final private List<BlockPos> affectedBlocks;
    
    @Redirect(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE",
            target = "Ljava/util/List;addAll(Ljava/util/Collection;)Z"))
    private boolean cancelAffectedBlocks(List list, Collection<?> c)
    {
        return false;
    }
    
    @Inject(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE",
            target = "Ljava/util/List;addAll(Ljava/util/Collection;)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void newAffectedBlocks(CallbackInfo ci, Set set_1)
    {
        if (!QuickCarpetSettings.getBool("explosionNoBlockDamage"))
        {
            this.affectedBlocks.addAll(set_1);
        }
    }
}
