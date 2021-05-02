package quickcarpet.mixin.movingBlockDuplicationFix;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TntBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.settings.Settings;
import quickcarpet.utils.PistonHelper;

@Mixin(TntBlock.class)
public class TntBlockMixin {
    @Inject(method = "neighborUpdate", at = @At("HEAD"), cancellable = true)
    private void fixDupe(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify, CallbackInfo ci) {
        if (Settings.tntDuplicationFix && PistonHelper.isBeingPushed(pos)) ci.cancel();
    }
}
