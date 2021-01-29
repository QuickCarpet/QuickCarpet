package quickcarpet.mixin.drownedNavigationFix;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.utils.extensions.ExtendedMobEntity;

import java.util.Set;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Redirect(method = "loadEntityUnchecked", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z", remap = false))
    private boolean addEntityNavigation(Set<EntityNavigation> set, Object nav, Entity entity) {
        ((ExtendedMobEntity) entity).setSavedNavigation((EntityNavigation) nav);
        return set.add((EntityNavigation) nav);
    }

    @Inject(method = "unloadEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;getNavigation()Lnet/minecraft/entity/ai/pathing/EntityNavigation;"))
    private void setFromSavedNavigation(Entity mob, CallbackInfo ci) {
        ((ExtendedMobEntity) mob).reloadToSavedNavigation();
    }
}
