package quickcarpet.mixin.movingBlockDuplicationFix;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.settings.Settings;
import quickcarpet.utils.PistonHelper;

@Mixin(AbstractRailBlock.class)
public class AbstractRailBlockMixin {
    @Inject(method = "neighborUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/AbstractRailBlock;dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"), cancellable = true)
    private void fixDupe(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify, CallbackInfo ci) {
        if (Settings.railDuplicationFix && PistonHelper.isBeingPushed(pos)) ci.cancel();
    }
}
