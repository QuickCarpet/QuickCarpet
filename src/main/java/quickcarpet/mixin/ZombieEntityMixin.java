package quickcarpet.mixin;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnType;
import net.minecraft.entity.ai.goal.BreakDoorGoal;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.settings.Settings;

@Mixin(ZombieEntity.class)
public abstract class ZombieEntityMixin extends HostileEntity {

    public ZombieEntityMixin(EntityType<? extends ZombieEntityMixin> entityType_1, World world_1) {
        super(entityType_1, world_1);
        this.breakDoorsGoal = new BreakDoorGoal(this, field_19015);
    }

    @Override
    protected void convertTo(EntityType<? extends ZombieEntity> entityType_1) {
        if (!this.removed) {
            ZombieEntity zombieEntity_1 = (ZombieEntity)entityType_1.create(this.world);
            zombieEntity_1.copyPositionAndRotation(this);
            zombieEntity_1.setCanPickUpLoot(this.canPickUpLoot());
            zombieEntity_1.setCanBreakDoors(zombieEntity_1.shouldBreakDoors() && this.canBreakDoors());
            zombieEntity_1.method_7205(zombieEntity_1.world.getLocalDifficulty(new BlockPos(zombieEntity_1)).getClampedLocalDifficulty());
            zombieEntity_1.setChild(this.isBaby());
            zombieEntity_1.setAiDisabled(this.isAiDisabled());
            EquipmentSlot[] var3 = EquipmentSlot.values();
            int var4 = var3.length;
            
            if (entityType_1 != EntityType.DROWNED) { //stop drowns from gaining any items when converted from zombies
                for(int var5 = 0; var5 < var4; ++var5) {
                    EquipmentSlot equipmentSlot_1 = var3[var5];
                    ItemStack itemStack_1 = this.getEquippedStack(equipmentSlot_1);
                    if (!itemStack_1.isEmpty()) {
                        zombieEntity_1.setEquippedStack(equipmentSlot_1, itemStack_1);
                        zombieEntity_1.setEquipmentDropChance(equipmentSlot_1, this.getDropChance(equipmentSlot_1));
                    }
                }
            }

            if (this.hasCustomName()) {
                zombieEntity_1.setCustomName(this.getCustomName());
                zombieEntity_1.setCustomNameVisible(this.isCustomNameVisible());
            }

            this.world.spawnEntity(zombieEntity_1);
            this.remove();
        }
    }
}
