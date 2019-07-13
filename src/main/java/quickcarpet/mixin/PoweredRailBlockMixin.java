package quickcarpet.mixin;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.PoweredRailBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import quickcarpet.annotation.Feature;

@Feature("railPowerLimit")
@Mixin(PoweredRailBlock.class)
public abstract class PoweredRailBlockMixin extends AbstractRailBlock {
    public PoweredRailBlockMixin(Block.Settings settings) {
        super(true, settings);
        throw new AssertionError();
    }

    @ModifyConstant(
            method = "isPoweredByOtherRails(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;ZI)Z",
            constant = @Constant(intValue = 8)
    )
    private int adjustPowerLimit(int powerLimit) {
        return quickcarpet.settings.Settings.railPowerLimit - 1;
    }
}
