package quickcarpet.mixin.drownedNavigationFix;

import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import quickcarpet.utils.extensions.ExtendedMobEntity;

@Mixin(MobEntity.class)
public class MobEntityMixin implements ExtendedMobEntity {
    private EntityNavigation savedNavigation;

    @Override
    public void setSavedNavigation(EntityNavigation navigation) {
        this.savedNavigation = navigation;
    }

    @Override
    public EntityNavigation getSavedNavigation() {
        return this.savedNavigation;
    }
}
