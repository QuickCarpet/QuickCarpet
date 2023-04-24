package quickcarpet.feature.dispenser;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;

public class BowlBowlablesBehavior extends FallibleItemDispenserBehavior {
    public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        BlockPos blockPos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
        List<LivingEntity> list = pointer.getWorld().getEntitiesByClass(LivingEntity.class, new Box(blockPos), entity -> entity instanceof MooshroomEntity);
        ItemStack mushroomStew = new ItemStack(Items.MUSHROOM_STEW);
        if(!list.isEmpty() && !list.get(0).isBaby()) {
            stack.decrement(1);
            if(stack.isEmpty()) {
                return mushroomStew;
            } else {
                if(((DispenserBlockEntity) pointer.getBlockEntity()).addToFirstFreeSlot(mushroomStew) < 0){
                    super.dispenseSilently(pointer, mushroomStew);
                }
            }
            this.setSuccess(true);
        }

        return stack;
    }
}
