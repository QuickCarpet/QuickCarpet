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
import quickcarpet.logging.LoggerRegistry;
import quickcarpet.logging.loghelpers.TNTLogHelper;

@Mixin(TntEntity.class)
public abstract class TntEntityMixin extends Entity {
    private TNTLogHelper logHelper = null;

    public TntEntityMixin(EntityType<?> entityType_1, World world_1) {
        super(entityType_1, world_1);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/entity/LivingEntity;)V",
            at = @At(value = "RETURN"))
    private void initTNTLogger(World world_1, double double_1, double double_2, double double_3,
                               LivingEntity livingEntity_1, CallbackInfo ci) {
        double double_4 = world_1.random.nextDouble() * 6.2831854820251465D;
        if (LoggerRegistry.TNT.isActive()) {
            logHelper = new TNTLogHelper();
            logHelper.onPrimed(double_1, double_2, double_3, double_4);
        }
    }

    @Inject(method = "explode", at = @At(value = "HEAD"))
    private void onExplode(CallbackInfo ci) {
        if (LoggerRegistry.TNT.isActive() && logHelper != null)
            logHelper.onExploded(x, y, z);
    }
}
