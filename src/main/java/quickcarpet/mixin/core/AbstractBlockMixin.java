package quickcarpet.mixin.core;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.utils.extensions.DynamicBlockEntityProvider;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockMixin {
    @Shadow public abstract Block getBlock();

    @Inject(method = "hasBlockEntity", at = @At("HEAD"), cancellable = true)
    private void quickcarpet$dynamicBlockEntity$hasBlockEntity(CallbackInfoReturnable<Boolean> cir) {
        Block block = getBlock();
        if (block instanceof DynamicBlockEntityProvider) {
            cir.setReturnValue(((DynamicBlockEntityProvider) block).quickcarpet$providesBlockEntity());
        }
    }
}
