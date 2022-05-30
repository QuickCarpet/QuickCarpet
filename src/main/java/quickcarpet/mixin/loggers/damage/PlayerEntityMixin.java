package quickcarpet.mixin.loggers.damage;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.logging.DamageLogHelper;

import static quickcarpet.utils.Messenger.dbl;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Shadow public abstract boolean isInvulnerableTo(DamageSource damageSource);

    @Unique
    private float damagePreDifficulty;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getDifficulty()Lnet/minecraft/world/Difficulty;", ordinal = 0))
    private void quickcarpet$log$damage$savePreDifficulty(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        damagePreDifficulty = amount;
    }

    @Inject(method = "damage", at = @At(value = "CONSTANT", args = "floatValue=0", ordinal = 1))
    private void quickcarpet$log$damage$modify$difficulty(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.isScaledWithDifficulty()) {
            DamageLogHelper.modify(this, source, damagePreDifficulty, amount, "difficulty");
        }
    }

    @Inject(method = "applyDamage", at = @At("HEAD"))
    private void quickcarpet$log$damage$modify$invulnerable(DamageSource source, float amount, CallbackInfo ci) {
        if (isInvulnerableTo(source)) {
            DamageLogHelper.modify(this, source, amount, 0, "invulnerable");
        }
    }

    @Redirect(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;applyArmorToDamage(Lnet/minecraft/entity/damage/DamageSource;F)F"))
    private float quickcarpet$log$damage$modify$armor(PlayerEntity player, DamageSource source, float amount) {
        float after = applyArmorToDamage(source, amount);
        DamageLogHelper.modify(this, source, amount, after, "armor", dbl(this.getArmor()), dbl(this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue()));
        return after;
    }

    @Inject(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setAbsorptionAmount(F)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void quickcarpet$log$damage$modify$absorption(DamageSource source, float amount, CallbackInfo ci, float after) {
        DamageLogHelper.modify(this, source, amount, after, "absorption");
    }

    @Inject(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setHealth(F)V"))
    private void quickcarpet$log$damage$final(DamageSource source, float amount, CallbackInfo ci) {
        DamageLogHelper.registerFinal(this, source, amount);
    }
}
