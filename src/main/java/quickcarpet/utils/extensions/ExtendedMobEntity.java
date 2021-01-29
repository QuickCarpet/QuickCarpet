package quickcarpet.utils.extensions;

import net.minecraft.entity.ai.pathing.EntityNavigation;

public interface ExtendedMobEntity {
    void setSavedNavigation(EntityNavigation navigation);
    EntityNavigation getSavedNavigation();
    void reloadToSavedNavigation();
}
