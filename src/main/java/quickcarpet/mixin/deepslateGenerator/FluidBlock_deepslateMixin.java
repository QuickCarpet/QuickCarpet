package quickcarpet.mixin.deepslateGenerator;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import quickcarpet.settings.Settings;

@Mixin(FluidBlock.class)
public class FluidBlock_deepslateMixin {

    @Redirect(
            method="receiveNeighborFluids(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z",
            at=@At(
                    value="INVOKE",
                    target="Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z"
            ))
    private boolean modifyCobblestone(World world, BlockPos pos, BlockState state) {
        if (Settings.deepslateGenerator && pos.getY() <= 0 && state.getBlock() == Blocks.COBBLESTONE) {
            if (pos.getY() < -7 || world.getRandom().nextInt(8+pos.getY()) == 0) {
                state = Blocks.COBBLED_DEEPSLATE.getDefaultState();
            }
        }
        return world.setBlockState(pos,state);
    }

}
