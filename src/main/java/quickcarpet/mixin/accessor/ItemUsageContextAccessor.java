package quickcarpet.mixin.accessor;

import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemUsageContext.class)
public interface ItemUsageContextAccessor {
    @Accessor("hit")
    void setHitResult(BlockHitResult hit);

    @Accessor("hit")
    BlockHitResult getHitResult();
}
