package quickcarpet.feature.dispenser;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;

public class UseNameTagsBehavior extends FallibleItemDispenserBehavior {
    public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        BlockPos blockPos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
        List<LivingEntity> list = pointer.getWorld().getEntitiesByClass(LivingEntity.class, new Box(blockPos), (entity -> entity instanceof LivingEntity));
        if(!list.isEmpty() && stack.hasCustomName() && !(list.get(0) instanceof PlayerEntity)) {
            Entity entity = list.get(0);
            entity.setCustomName(stack.getName());
            if(entity instanceof MobEntity){
              ((MobEntity) entity).setPersistent();
            }
            stack.decrement(1);
            this.setSuccess(true);
        } else {
            this.setSuccess(false);
        }
        return stack;
    }
}
