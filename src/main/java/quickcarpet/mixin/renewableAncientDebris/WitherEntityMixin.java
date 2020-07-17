package quickcarpet.mixin.renewableAncientDebris;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import quickcarpet.settings.Settings;

@Mixin(WitherEntity.class)
public abstract class WitherEntityMixin extends HostileEntity {
    protected WitherEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }
    
    @Override
    public void onDeath(DamageSource source) {
        if (this.random.nextDouble() <= 0.01 && Settings.renewableAncientDebris) {
            this.dropStack(new ItemStack(Items.ANCIENT_DEBRIS));
        }
        super.onDeath(source);
    }
}
