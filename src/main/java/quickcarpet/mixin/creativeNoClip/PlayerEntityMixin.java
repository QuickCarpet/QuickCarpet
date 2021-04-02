package quickcarpet.mixin.creativeNoClip;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.utils.Utils;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(method = {"tick", "updatePose"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSpectator()Z"))
    private boolean noClip(PlayerEntity player) {
        return Utils.isNoClip(player);
    }

    @Override
    public void move(MovementType type, Vec3d movement) {
        if (type != MovementType.SELF && Utils.isNoClip((PlayerEntity) (Object) this)) return;
        super.move(type, movement);
    }
}
