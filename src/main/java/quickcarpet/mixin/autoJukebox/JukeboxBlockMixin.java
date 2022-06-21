package quickcarpet.mixin.autoJukebox;

import net.minecraft.block.BlockState;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.mixin.accessor.JukeboxBlockEntityAccessor;
import quickcarpet.settings.Settings;

@Mixin(JukeboxBlock.class)
public class JukeboxBlockMixin {
    @Inject(method = "getComparatorOutput", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/JukeboxBlockEntity;getRecord()Lnet/minecraft/item/ItemStack;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void quickcarpet$autoJukebox$comparatorOnlyWhenPlaying(BlockState state, World world, BlockPos pos, CallbackInfoReturnable<Integer> cir, BlockEntity blockEntity) {
        if (Settings.autoJukebox && !((JukeboxBlockEntityAccessor) blockEntity).getIsPlaying()) {
            cir.setReturnValue(0);
        }
    }
}
