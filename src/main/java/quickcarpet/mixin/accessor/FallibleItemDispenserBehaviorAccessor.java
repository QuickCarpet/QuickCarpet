package quickcarpet.mixin.accessor;

import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FallibleItemDispenserBehavior.class)
public interface FallibleItemDispenserBehaviorAccessor {
    @Accessor("success")
    boolean isSuccessful();
}
