package quickcarpet.mixin.fillUpdates;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import quickcarpet.settings.Settings;

import static quickcarpet.utils.Constants.SetBlockState.NO_FILL_UPDATE;

@Mixin(StructureTemplate.class)
public abstract class StructureTemplateMixin {
    @Redirect(method = "place", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/ServerWorldAccess;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/util/Clearable;clear(Ljava/lang/Object;)V"),
                    to = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I")))
    private boolean quickcarpet$fillUpdates$newSetBlockStateOne(ServerWorldAccess world, BlockPos pos, BlockState state, int flags) {
        return world.setBlockState(pos, state, flags | (Settings.fillUpdates ? 0 : NO_FILL_UPDATE));
    }

    @Redirect(method = "place", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/ServerWorldAccess;updateNeighbors(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;)V"))
    private void quickcarpet$fillUpdates$ifUpdateNeighbours(ServerWorldAccess world, BlockPos var1, Block var2) {
        if (Settings.fillUpdates) {
            world.updateNeighbors(var1, var2);
        }
    }
}
