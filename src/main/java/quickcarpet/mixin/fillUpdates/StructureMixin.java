package quickcarpet.mixin.fillUpdates;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.structure.Structure;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import quickcarpet.annotation.Feature;
import quickcarpet.settings.Settings;

import static quickcarpet.utils.Constants.SetBlockState.NO_FILL_UPDATE;

@Feature("fillUpdates")
@Mixin(Structure.class)
public abstract class StructureMixin {
    @Redirect(method = "method_15172", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/IWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/util/Clearable;clear(Ljava/lang/Object;)V"),
                    to = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I")))
    private boolean newSetBlockStateOne(IWorld world, BlockPos pos, BlockState state, int flags) {
        return world.setBlockState(pos, state, flags | (Settings.fillUpdates ? 0 : NO_FILL_UPDATE));
    }

    @Redirect(method = "method_15172", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/IWorld;updateNeighbors(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;)V"))
    private void ifUpdateNeighbours(IWorld iWorld, BlockPos var1, Block var2) {
        if (Settings.fillUpdates) {
            iWorld.updateNeighbors(var1, var2);
        }
    }
}
