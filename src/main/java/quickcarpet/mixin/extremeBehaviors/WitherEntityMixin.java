package quickcarpet.mixin.extremeBehaviors;

import net.minecraft.entity.boss.WitherEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import quickcarpet.settings.Settings;

@Mixin(WitherEntity.class)
public class WitherEntityMixin {
    @ModifyConstant(method = "shootSkullAt(ILnet/minecraft/entity/LivingEntity;)V", constant = @Constant(floatValue = 0.001f))
    private float extremeBehaviors(float original) {
        return Settings.extremeBehaviors ? 0.1f : original;
    }
}
