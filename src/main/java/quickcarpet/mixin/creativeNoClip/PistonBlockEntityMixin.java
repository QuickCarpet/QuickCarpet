package quickcarpet.mixin.creativeNoClip;

import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.utils.Utils;

@Mixin(PistonBlockEntity.class)
public class PistonBlockEntityMixin {
    @Redirect(method = "pushEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setVelocity(DDD)V"))
    private static void quickcarpet$creativeNoClip$setVelocity(Entity entity, double x, double y, double z) {
        if (entity instanceof PlayerEntity && Utils.isNoClip(((PlayerEntity) entity))) return;
        entity.setVelocity(x, y, z);
    }
}
