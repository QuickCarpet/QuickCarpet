package quickcarpet.mixin.updateSuppressionCrashFix;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater;
import net.minecraft.world.block.NeighborUpdater;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.settings.Settings;

@Mixin(ChainRestrictedNeighborUpdater.SixWayEntry.class)
public class SixWayEntryMixin {
    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;neighborUpdate(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;Z)V"))
    private void quickcarpet$updateSuppressionCrashFix$tryNeighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (Settings.updateSuppressionCrashFix) {
            NeighborUpdater.tryNeighborUpdate(world, state, pos, sourceBlock, sourcePos, notify);
        } else {
            state.neighborUpdate(world, pos, sourceBlock, sourcePos, notify);
        }
    }
}
