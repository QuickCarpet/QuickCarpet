package quickcarpet.mixin.tnt;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.logging.Loggers;
import quickcarpet.logging.TNTLogHelper;
import quickcarpet.settings.Settings;

@Mixin(TntEntity.class)
public abstract class TntEntityMixin extends Entity {
    private TNTLogHelper logHelper = null;

    public TntEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/entity/LivingEntity;)V", at = @At("RETURN"))
    private void quickcarpet$tnt$onInit(World world, double x, double y, double z, LivingEntity activator, CallbackInfo ci) {
        if (!Settings.tntPrimeMomentum) {
            setVelocity(0, 0.2, 0);
        } else if (Settings.tntHardcodeAngle >= 0) {
            double rad = -Math.toRadians(Settings.tntHardcodeAngle);
            setVelocity(Math.sin(rad) * 0.02, 0.2, Math.cos(rad) * 0.02);
        }
        if (Loggers.TNT.isActive()) logHelper = new TNTLogHelper((TntEntity) (Object) this);
    }

    @Inject(method = "explode", at = @At(value = "HEAD"))
    private void quickcarpet$log$tnt$onExplode(CallbackInfo ci) {
        if (logHelper != null) {
            logHelper.onExploded();
            logHelper = null;
        }
    }
}
