package quickcarpet.mixin.renewableDeepslate;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import quickcarpet.settings.Settings;

@Mixin(FluidBlock.class)
public class FluidBlockMixin {

    @Redirect(
            method="receiveNeighborFluids(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z",
            at=@At(
                    value="INVOKE",
                    target="Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z"
            ))
    private boolean modifyCobblestone(World world, BlockPos pos, BlockState state) {
        if (Settings.renewableDeepslate && pos.getY() <= 16 && state.getBlock() == Blocks.COBBLESTONE) {
            if (pos.getY() < -7 || world.getRandom().nextFloat() >= (8+pos.getY())/24.0) {
                state = Blocks.COBBLED_DEEPSLATE.getDefaultState();
            }
        }
        return world.setBlockState(pos,state);
    }

}
