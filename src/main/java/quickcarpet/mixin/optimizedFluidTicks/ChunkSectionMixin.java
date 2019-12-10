package quickcarpet.mixin.optimizedFluidTicks;

import net.minecraft.fluid.FluidState;
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.annotation.Feature;
import quickcarpet.settings.Settings;

@Feature("optimizedFluidTicks")
@Mixin(ChunkSection.class)
public class ChunkSectionMixin {
    @Redirect(method = "setBlockState(IIILnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;", at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;isEmpty()Z"))
    private boolean fixRandomFluidCount(FluidState fluidState) {
        if (Settings.optimizedFluidTicks) return fluidState.hasRandomTicks();
        return fluidState.isEmpty();
    }
}
