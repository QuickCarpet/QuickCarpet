package quickcarpet.mixin.xpCoolDown;

import net.minecraft.entity.ExperienceOrbEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import quickcarpet.settings.Settings;

@Mixin(ExperienceOrbEntity.class)
public class ExperienceOrbEntityMixin {
    @ModifyConstant(method = "onPlayerCollision", constant = @Constant(intValue = 2))
    private int setPickupDelay(int value) {
        return Settings.xpCoolDown;
    }
}
