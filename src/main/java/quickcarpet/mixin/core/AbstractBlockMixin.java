package quickcarpet.mixin.core;

import net.minecraft.block.AbstractBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.utils.extensions.DynamicBlockEntityProvider;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {
    @Inject(method = "hasBlockEntity", at = @At("HEAD"), cancellable = true)
    private void betterHasBlockEntity(CallbackInfoReturnable<Boolean> cir) {
        if (this instanceof DynamicBlockEntityProvider) cir.setReturnValue(((DynamicBlockEntityProvider) this).providesBlockEntity());
    }
}
