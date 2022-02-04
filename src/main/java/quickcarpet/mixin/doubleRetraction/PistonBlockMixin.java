package quickcarpet.mixin.doubleRetraction;

import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonBlock.class)
public class PistonBlockMixin {
    @Inject(method = "tryMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addSyncedBlockEvent(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V", ordinal = 1, shift = At.Shift.BEFORE))
    private void quickcarpet$doubleRetraction(World world, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (quickcarpet.settings.Settings.doubleRetraction) {
            world.setBlockState(pos, state.with(PistonBlock.EXTENDED, false), 2);
        }
    }
}
