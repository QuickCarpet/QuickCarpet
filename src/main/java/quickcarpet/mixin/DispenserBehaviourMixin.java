package quickcarpet.mixin;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Items;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.feature.TillSoilDispenserBehaviour;

@Mixin(DispenserBehavior.class)
public interface DispenserBehaviourMixin
{
    @SuppressWarnings("PublicStaticMixinMember")
    @Inject(method = "registerDefaults", at = @At("TAIL"))
    static void onRegisterDefaults(CallbackInfo ci)
    {
        Registry.ITEM.forEach(hoe -> {
            if (hoe instanceof HoeItem)
                DispenserBlock.registerBehavior(hoe, new TillSoilDispenserBehaviour());
        });
    }
}
