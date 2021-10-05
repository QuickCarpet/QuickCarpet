package quickcarpet.mixin.renewableDeepslate;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.settings.Settings;

@Mixin(LavaFluid.class)
public class LavaFluidMixin {

    @Redirect(
            method= "flow(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;Lnet/minecraft/fluid/FluidState;)V",
            at=@At(
                    value="INVOKE",
                    target="Lnet/minecraft/world/WorldAccess;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"
            ))
    private boolean modifyStone(WorldAccess worldAccess, BlockPos pos, BlockState state, int flags) {
        if (Settings.renewableDeepslate && pos.getY() <= 16) {
            if (pos.getY() < -7 || worldAccess.getRandom().nextFloat() >= (8+pos.getY())/24.0) {
                state = Blocks.DEEPSLATE.getDefaultState();
            }
        }
        return worldAccess.setBlockState(pos,state,flags);
    }
}
