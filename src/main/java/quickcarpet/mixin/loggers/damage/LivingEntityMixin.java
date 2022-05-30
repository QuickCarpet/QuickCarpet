package quickcarpet.mixin.loggers.damage;

import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.logging.DamageLogHelper;

import static quickcarpet.utils.Messenger.dbl;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow protected float lastDamageTaken;
    @Shadow public abstract int getArmor();
    @Shadow public abstract @Nullable EntityAttributeInstance getAttributeInstance(EntityAttribute attribute);
    @Shadow protected abstract float applyArmorToDamage(DamageSource source, float amount);

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isDead()Z", ordinal = 0))
    private void quickcarpet$log$damage$register(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageLogHelper.register((LivingEntity) (Object) this, source, amount);
    }

    @Inject(method = "damage", at = @At(value = "RETURN", ordinal = 2))
    private void quickcarpet$log$damage$modify$dead(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageLogHelper.modify((LivingEntity) (Object) this, source, amount, 0, "dead");
    }

    @Inject(method = "damage", at = @At(value = "RETURN", ordinal = 3))
    private void quickcarpet$log$damage$modify$fireResistance(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageLogHelper.modify((LivingEntity) (Object) this, source, amount, 0, "fireResistance");
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damageShield(F)V"))
    private void quickcarpet$log$damage$modify$shield(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageLogHelper.modify((LivingEntity) (Object) this, source, amount, 0, "shield");
    }

    @Inject(method = "damage", at = @At(value = "CONSTANT", args = "floatValue=0.75"))
    private void quickcarpet$log$damage$modify$helmet(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageLogHelper.modify((LivingEntity) (Object) this, source, amount, amount * 0.75f, "helmet");
    }

    @Inject(method = "damage", at = @At("RETURN"), slice = @Slice(
            from = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;timeUntilRegen:I", ordinal = 0),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", ordinal = 0)
    ))
    private void quickcarpet$log$damage$modify$cooldownFull(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageLogHelper.modify((LivingEntity) (Object) this, source, amount, 0, "cooldown");
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", ordinal = 0))
    private void quickcarpet$log$damage$modify$cooldownPart(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageLogHelper.modify((LivingEntity) (Object) this, source, amount, amount - lastDamageTaken, "cooldown");
    }

    @Inject(method = "applyEnchantmentsToDamage", at = @At(value = "CONSTANT", args = "floatValue=25"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void quickcarpet$log$damage$modify$resistanceEffect(DamageSource source, float damage, CallbackInfoReturnable<Float> cir, int i, int j, float f) {
        DamageLogHelper.modify((LivingEntity) (Object) this, source, damage, f / 25.0F, "resistanceEffect");
    }

    @Redirect(method = "applyEnchantmentsToDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/DamageUtil;getInflictedDamage(FF)F"))
    private float quickcarpet$log$damage$modify$enchantments(float damage, float enchantModifiers, DamageSource source) {
        float after = DamageUtil.getInflictedDamage(damage, enchantModifiers);
        DamageLogHelper.modify((LivingEntity) (Object) this, source, damage, after, "enchantments", dbl(enchantModifiers));
        return after;
    }

    @Redirect(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyArmorToDamage(Lnet/minecraft/entity/damage/DamageSource;F)F"))
    private float quickcarpet$log$damage$modify$armor(LivingEntity entity, DamageSource source, float amount) {
        float after = applyArmorToDamage(source, amount);
        DamageLogHelper.modify((LivingEntity) (Object) this, source, amount, after, "armor", dbl(this.getArmor()), dbl(this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue()));
        return after;
    }

    @Inject(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setAbsorptionAmount(F)V", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void quickcarpet$log$damage$modify$absorption(DamageSource source, float amount, CallbackInfo ci, float after) {
        DamageLogHelper.modify((LivingEntity) (Object) this, source, amount, after, "absorption");
    }

    @Inject(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setHealth(F)V"))
    private void quickcarpet$log$damage$final(DamageSource source, float amount, CallbackInfo ci) {
        DamageLogHelper.registerFinal((LivingEntity) (Object) this, source, amount);
    }
}
