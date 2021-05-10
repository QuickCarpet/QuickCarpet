package quickcarpet.mixin.piston;

import net.minecraft.block.piston.PistonBehavior;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Unique;
import quickcarpet.utils.PistonHelper;

import java.util.Arrays;

/**
 * @author 2No2Name, skyrising
 */
@Mixin(PistonBehavior.class)
class PistonBehaviorMixin {
    private static @Mutable @Final PistonBehavior[] field_15973;

    private PistonBehaviorMixin(String name, int ordinal) {}

    static {
        @SuppressWarnings("ConstantConditions")
        int i = field_15973.length;
        field_15973 = Arrays.copyOf(field_15973, i + 2);
        PistonHelper.WEAK_STICKY = add(new PistonBehaviorMixin("WEAK_STICKY", i));
        PistonHelper.WEAK_STICKY_BREAKABLE = add(new PistonBehaviorMixin("WEAK_STICKY_BREAKABLE", i + 1));
    }

    @Unique
    private static PistonBehavior add(Object behavior) {
        // Casts from the first argument to the target will be stripped (bug SpongePowered/Mixin#471)
        // so we need to first move it into local #1
        @SuppressWarnings("UnnecessaryLocalVariable")
        Object o = behavior;
        PistonBehavior p = (PistonBehavior) o;
        field_15973[p.ordinal()] = p;
        return p;
    }
}
