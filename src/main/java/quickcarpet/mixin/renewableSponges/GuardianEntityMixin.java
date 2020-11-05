package quickcarpet.mixin.renewableSponges;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import quickcarpet.settings.Settings;

@Mixin(GuardianEntity.class)
public class GuardianEntityMixin extends HostileEntity {
    protected GuardianEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity lightningStrike) {
        if (!Settings.renewableSponges) {
            super.onStruckByLightning(world, lightningStrike);
            return;
        }
        ElderGuardianEntity elderGuardian = EntityType.ELDER_GUARDIAN.create(world);
        elderGuardian.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.yaw, this.pitch);
        elderGuardian.initialize(world, world.getLocalDifficulty(elderGuardian.getBlockPos()), SpawnReason.CONVERSION, null, null);
        elderGuardian.setAiDisabled(this.isAiDisabled());
        if (this.hasCustomName()) {
            elderGuardian.setCustomName(this.getCustomName());
            elderGuardian.setCustomNameVisible(this.isCustomNameVisible());
        }
        this.method_31472();
        world.spawnEntityAndPassengers(elderGuardian);
    }
}
