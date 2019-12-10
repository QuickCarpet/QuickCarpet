package quickcarpet.mixin.fillUpdates;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import quickcarpet.annotation.Feature;

import static quickcarpet.utils.Constants.SetBlockState.NO_FILL_UPDATE;
import static quickcarpet.utils.Constants.SetBlockState.NO_OBSERVER_UPDATE;

@Feature("fillUpdates")
@Mixin(World.class)
public class WorldMixin {
    @ModifyConstant(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z",
            constant = @Constant(intValue = NO_OBSERVER_UPDATE))
    private int addFillUpdatesInt(int original) {
        return NO_OBSERVER_UPDATE | NO_FILL_UPDATE;
    }
}
