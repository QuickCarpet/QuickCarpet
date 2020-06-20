package quickcarpet.mixin.tnt;

import net.minecraft.block.Block;
import net.minecraft.block.TntBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TntBlock.class)
public class TntBlockMixin extends Block {
    public TntBlockMixin(Settings settings) {
        super(settings);
    }

    @Redirect(method = "onBlockAdded", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isReceivingRedstonePower(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean tntUpdateOnPlace(World world, BlockPos pos) {
        if (!quickcarpet.settings.Settings.tntUpdateOnPlace) return false;
        return world.isReceivingRedstonePower(pos);
    }
}
