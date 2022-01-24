package quickcarpet.mixin.creativeNoClip;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.utils.Utils;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @Redirect(method = "canPlace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;canPlace(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Z"))
    private boolean canPlaceNoClip(World world, BlockState state, BlockPos pos, ShapeContext context, ItemPlacementContext context1) {
        PlayerEntity player = context1.getPlayer();
        if (player != null && Utils.isNoClip(player)) {
            VoxelShape shape = state.getCollisionShape(world, pos, context);
            return shape.isEmpty() || world.doesNotIntersectEntities(player, shape.offset(pos.getX(), pos.getY(), pos.getZ()));
        }
        return world.canPlace(state, pos, context);
    }
}
