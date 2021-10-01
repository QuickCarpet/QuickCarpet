package quickcarpet.mixin.deepslateGenerator;

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
public class LavaFluid_deepslateMixin {

    @Redirect(
            method= "flow(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;Lnet/minecraft/fluid/FluidState;)V",
            at=@At(
                    value="INVOKE",
                    target="Lnet/minecraft/world/WorldAccess;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"
            ))
    private boolean modifyStone(WorldAccess worldAccess, BlockPos pos, BlockState state, int flags) {
        if (Settings.deepslateGenerator && pos.getY() <= 0) {
            if (pos.getY() < -7 || worldAccess.getRandom().nextInt(8+pos.getY()) == 0) {
                state = Blocks.DEEPSLATE.getDefaultState();
            }
        }
        return worldAccess.setBlockState(pos,state,flags);
    }
}
