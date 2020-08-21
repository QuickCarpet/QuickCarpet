package quickcarpet.mixin.xp;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.settings.Settings;

import static net.minecraft.entity.ExperienceOrbEntity.roundToOrbSize;

@Mixin(ExperienceOrbEntity.class)
public abstract class ExperienceOrbEntityMixin extends Entity {
    private static final int MERGE_COOLDOWN = 50;

    @Shadow public int pickupDelay;
    @Shadow private int amount;

    @Shadow public int orbAge;
    private int mergeDelay = MERGE_COOLDOWN;

    public ExperienceOrbEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    public boolean canMerge() {
        return Settings.xpMerging && this.isAlive() && pickupDelay != 32767 && orbAge < 6000 && amount < 2477;
    }

    @ModifyConstant(method = "onPlayerCollision", constant = @Constant(intValue = 2))
    private int getCoolDown(int coolDown) {
        return Settings.xpCoolDown;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ExperienceOrbEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V", shift = At.Shift.AFTER))
    private void tryCombine(CallbackInfo ci) {
        if (mergeDelay == 0) {
            tryMerge();
            mergeDelay = MERGE_COOLDOWN;
        } else {
            mergeDelay--;
        }
    }

    @SuppressWarnings("ConstantConditions")
    private boolean canMergeWith(ExperienceOrbEntity other) {
        ExperienceOrbEntityMixin o = (ExperienceOrbEntityMixin) (Object) other;
        return o != this && o.canMerge() && amount >= o.amount;
    }

    private void tryMerge() {
        if (world.isClient() || !canMerge()) return;
        for (ExperienceOrbEntity other : world.getEntitiesByClass(ExperienceOrbEntity.class, getBoundingBox().expand(0.5, 0, 0.5), this::canMergeWith)) {
            merge(other);
            return;
        }
    }

    private void merge(ExperienceOrbEntity other) {
        int total = other.getExperienceAmount() + amount;
        int newAmount = Math.min(total, 2477);
        if (total > newAmount) {
            int newOtherAmount = total - newAmount;
            boolean sizeChanged = roundToOrbSize(other.getExperienceAmount()) != roundToOrbSize(newOtherAmount);
            //noinspection ConstantConditions
            ExperienceOrbEntityMixin other2 = ((ExperienceOrbEntityMixin) (Object) other);
            other2.amount = newOtherAmount;
            other2.mergeDelay = MERGE_COOLDOWN;
            if (sizeChanged) respawnOrb(other);
        } else {
            other.remove();
        }
        boolean sizeChanged = roundToOrbSize(amount) != roundToOrbSize(newAmount);
        amount = newAmount;
        pickupDelay = Math.max(pickupDelay, other.pickupDelay);
        orbAge = Math.min(orbAge, other.orbAge);
        mergeDelay = MERGE_COOLDOWN;
        if (sizeChanged) respawnOrb((ExperienceOrbEntity) (Object) this);
    }

    private void respawnOrb(ExperienceOrbEntity orb) {
        orb.remove();
        ExperienceOrbEntity newOrb = new ExperienceOrbEntity(orb.world, orb.getX(), orb.getY(), orb.getZ(), orb.getExperienceAmount());
        newOrb.yaw = orb.yaw;
        newOrb.pitch = orb.pitch;
        newOrb.setVelocity(orb.getVelocity());
        newOrb.orbAge = orb.orbAge;
        newOrb.pickupDelay = orb.pickupDelay;
        orb.world.spawnEntity(newOrb);
    }
}
