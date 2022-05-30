package quickcarpet.mixin.loggers.damage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.logging.DamageLogHelper;

@Mixin(MobEntity.class)
public class MobEntityMixin {
    @Unique
    private float attackDamagePre;

    @Inject(method = "tryAttack", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/entity/mob/MobEntity;getAttributeValue(Lnet/minecraft/entity/attribute/EntityAttribute;)D",
        ordinal = 1
    ), locals = LocalCapture.CAPTURE_FAILHARD)
    private void quickcarpet$log$damage$registerAttacker(Entity target, CallbackInfoReturnable<Boolean> cir, float attackDamage) {
        DamageLogHelper.registerAttacker(target, (MobEntity) (Object) this, attackDamage);
        attackDamagePre = attackDamage;
    }

    @Inject(method = "tryAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;getKnockback(Lnet/minecraft/entity/LivingEntity;)I"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void quickcarpet$log$damage$modify$attackerEnchantments(Entity entity, CallbackInfoReturnable<Boolean> cir, float attackDamage) {
        DamageLogHelper.modify((LivingEntity)entity, DamageSource.mob((MobEntity) (Object) this), attackDamagePre, attackDamage, "attackerEnchantments");
    }
}
