package quickcarpet.mixin.nbtMotionLimit;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import quickcarpet.settings.Settings;

@Mixin(Entity.class)
public class EntityMixin {
    @ModifyConstant(method = "fromTag", constant = @Constant(doubleValue = 10))
    private double nbtMotionLimit(double limit) {
        double carpetLimit = Settings.nbtMotionLimit;
        if (carpetLimit <= 0) return Double.POSITIVE_INFINITY;
        return carpetLimit;
    }
}
