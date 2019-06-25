package quickcarpet.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.settings.Settings;

@Mixin(ZombieEntity.class)
public abstract class ZombieEntityMixin extends HostileEntity {
    protected ZombieEntityMixin(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
    }

    @Redirect(method = "convertTo", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/mob/ZombieEntity;removed:Z"))
    private boolean fixConversionDuping(ZombieEntity zombie) {
        return Settings.conversionDupingFix ? zombie.isAlive() : zombie.removed;
    }
}
