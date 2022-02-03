package quickcarpet.mixin.sparkingLighter;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.item.FireChargeItem;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.settings.Settings;

@Mixin({
    FlintAndSteelItem.class,
    FireChargeItem.class
})
public class LighterItemMixin {
    @Redirect(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/AbstractFireBlock;canPlaceAt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z"))
    private boolean quickcarpet$sparkingLighter$canPlaceFireAt(World world, BlockPos pos, Direction direction) {
        return Settings.sparkingLighter ? world.isAir(pos) : AbstractFireBlock.canPlaceAt(world, pos, direction);
    }
}
