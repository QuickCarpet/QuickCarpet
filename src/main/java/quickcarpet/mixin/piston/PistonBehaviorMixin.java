package quickcarpet.mixin.piston;

import net.minecraft.block.piston.PistonBehavior;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;
import quickcarpet.utils.PistonBehaviors;

/**
 * @author 2No2Name, skyrising
 */
@Mixin(PistonBehavior.class)
class PistonBehaviorMixin {
    private static @Final
    PistonBehavior[] field_15973;

    private PistonBehaviorMixin(String name, int ordinal) {}

    @SuppressWarnings("UnresolvedMixinReference")
    @ModifyConstant(method = "<clinit>()V", constant = @Constant(intValue = 5), slice = @Slice(from = @At("HEAD"), to = @At(value = "FIELD", opcode = Opcodes.GETSTATIC)))
    private static int valuesArraySize(int prev) {
        return 7;
    }

    static {
        field_15973[5] = (PistonBehavior) (Object) new PistonBehaviorMixin("WEAK_STICKY", 5);
        field_15973[6] = (PistonBehavior) (Object) new PistonBehaviorMixin("WEAK_STICKY_BREAKABLE", 6);
        PistonBehaviors.WEAK_STICKY_BREAKABLE = field_15973[6];
        PistonBehaviors.WEAK_STICKY = field_15973[5];
    }
}
