package quickcarpet.mixin.core;

import net.minecraft.block.BlockState;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Set;

@Mixin(PointOfInterestTypes.class)
public class PointOfInterestTypesMixin {
    @Shadow @Final protected static Set<BlockState> POI_STATES;
    @Shadow @Final private static Map<BlockState, RegistryEntry<PointOfInterestType>> POI_STATES_TO_TYPE;

    @Inject(method = "registerAndGetDefault", at = @At("RETURN"))
    private static void quickcarpet$poiFix$updateKeySet(Registry<PointOfInterestType> registry, CallbackInfoReturnable<PointOfInterestType> cir) {
        POI_STATES.addAll(POI_STATES_TO_TYPE.keySet());
    }
}
