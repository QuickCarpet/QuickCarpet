package quickcarpet.mixin.blockPlacement;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.annotation.Feature;
import quickcarpet.utils.AccurateBlockPlacement;

@Feature("accurateBlockPlacement")
@Mixin(BlockItem.class)
public class BlockItemMixin {
    @Redirect(method = "getPlacementState", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getPlacementState(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/block/BlockState;"))
    private BlockState adjustPlacementState(Block block, ItemPlacementContext ctx) {
        return AccurateBlockPlacement.getPlacementState(block, ctx);
    }
}
