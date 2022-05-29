package quickcarpet.mixin.loggers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.logging.Loggers;

import java.util.Iterator;
import java.util.List;

import static quickcarpet.utils.Constants.OtherKeys.*;
import static quickcarpet.utils.Messenger.s;
import static quickcarpet.utils.Messenger.t;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow public abstract String getEntityName();

    @Unique
    private int sweepingKills = 0;

    @Inject(method = "attack", at = @At("HEAD"))
    private void quickcarpet$log$kill$onAttackStart(Entity target, CallbackInfo ci) {
        sweepingKills = 0;
    }

    @Inject(method = "attack", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
        shift = At.Shift.AFTER
    ), locals = LocalCapture.CAPTURE_FAILHARD)
    private void quickcarpet$log$kills$onSweepEntity(
        Entity target, CallbackInfo ci,
        float f, float g, boolean bl, boolean bl2, int i, boolean bl3, boolean bl4,
        float itemStack, boolean bl5, int j, Vec3d vec3d, float k,
        List<LivingEntity> sweepingEntities, Iterator<LivingEntity> sweepingIterator, LivingEntity sweepingTarget
    ) {
        if (sweepingTarget.isDead()) {
            sweepingKills++;
        }
    }

    @Inject(method = "attack", at = @At("TAIL"))
    private void quickcarpet$log$kill$onAttackEnd(Entity target, CallbackInfo ci) {
        int total = sweepingKills + (target.isAlive() ? 0 : 1);
        if (total <= 0 || world.isClient) return;
        Loggers.KILLS.log(() -> {
            String killer = this.getEntityName();
            if (sweepingKills > 0 && total > 1) return t(KILLS_LOG_SWEEPING_N, killer, s(Integer.toString(total), Formatting.DARK_GREEN));
            if (sweepingKills > 0) return t(KILLS_LOG_SWEEPING_1, killer, s(Integer.toString(total), Formatting.DARK_GREEN));
            return t(KILLS_LOG_1, killer, s(Integer.toString(total), Formatting.DARK_GREEN));
        });
    }
}
