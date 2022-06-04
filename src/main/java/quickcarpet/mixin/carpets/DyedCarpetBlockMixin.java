package quickcarpet.mixin.carpets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DyedCarpetBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.DyeColor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import quickcarpet.utils.Carpets;

@Mixin(DyedCarpetBlock.class)
public class DyedCarpetBlockMixin extends Block {
    @Shadow @Final private DyeColor dyeColor;

    public DyedCarpetBlockMixin(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return Carpets.onPlace(ctx.getPlayer(), ctx.getWorld(), ctx.getBlockPos(), dyeColor, ctx.getHitPos()) ? null : super.getPlacementState(ctx);
    }
}
