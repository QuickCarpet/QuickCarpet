package quickcarpet.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.annotation.Feature;
import quickcarpet.logging.LoggerRegistry;
import quickcarpet.logging.loghelpers.TNTLogHelper;

@Feature("logger.tnt")
@Mixin(TntEntity.class)
public abstract class TntEntityMixin extends Entity {
    private TNTLogHelper logHelper = null;

    public TntEntityMixin(EntityType<?> entityType_1, World world_1) {
        super(entityType_1, world_1);
    }

    /* // Mixin bug https://github.com/FabricMC/Mixin/issues/23
    @Redirect(
            method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/entity/LivingEntity;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/Random;nextDouble()D")
    )
    private double initTNTLogger(Random random, World world, double x, double y, double z) {
        double nextDouble = random.nextDouble();
        if (LoggerRegistry.TNT.isActive()) {
            logHelper = new TNTLogHelper();
            logHelper.onPrimed(x, y, z, nextDouble * 2 * Math.PI);
        }
        return nextDouble;
    }
    */

    @Inject(method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/entity/LivingEntity;)V", at = @At("RETURN"))
    private void initTNTLogger(World world, double x, double y, double z, LivingEntity activator, CallbackInfo ci) {
        if (LoggerRegistry.TNT.isActive()) logHelper = new TNTLogHelper((TntEntity) (Object) this);
    }

    @Inject(method = "explode", at = @At(value = "HEAD"))
    private void onExplode(CallbackInfo ci) {
        if (logHelper != null) {
            logHelper.onExploded();
            logHelper = null;
        }
    }
}
