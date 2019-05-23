package quickcarpet.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.structure.Structure;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import quickcarpet.settings.Settings;

@Mixin(Structure.class)
public abstract class StructureMixin
{
    
    @Redirect(method = "method_15172", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/world/IWorld;" +
                             "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/util/Clearable;clear(Ljava/lang/Object;)V"),
                            to = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I")))
    private boolean newSetBlockStateOne(IWorld iWorld, BlockPos var1, BlockState var2, int var3)
    {
        return iWorld.setBlockState(var1, Blocks.BARRIER.getDefaultState(), 4 | (Settings.fillUpdates ? 0 : 1024));
    }
    
    @Redirect(method = "method_15172", at = @At(value = "INVOKE", ordinal = 1,
            target = "Lnet/minecraft/world/IWorld;" +
                             "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/util/Clearable;clear(Ljava/lang/Object;)V"),
                    to = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I")))
    private boolean newSetBlockStateTwo(IWorld iWorld, BlockPos var1, BlockState var2, int var3)
    {
        return iWorld.setBlockState(var1, var2, var3 | (Settings.fillUpdates ? 0 : 1024));
    }
    
    @Redirect(method = "method_15172", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/IWorld;updateNeighbors(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;)V"))
    private void ifUpdateNeighbours(IWorld iWorld, BlockPos var1, Block var2)
    {
        if (Settings.fillUpdates) {
            iWorld.updateNeighbors(var1, var2);
        }
    }
    
    
}
