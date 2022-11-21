package quickcarpet.mixin.fluidDamage;

import net.minecraft.block.Material;
import net.minecraft.fluid.FlowableFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.settings.Settings;

@Mixin(FlowableFluid.class)
public class FlowableFluidMixin {
    @Redirect(method = "canFill", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Material;blocksMovement()Z"))
    private boolean quickcarpet$fluidDamage$blocksMovement(Material material) {
        if (!Settings.fluidDamage && !material.isReplaceable()) {
            return true;
        }
        return material.blocksMovement();
    }
}
