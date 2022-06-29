package quickcarpet.feature.dispenser;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.GoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;

public class MilkCowsAndGoatsBehavior extends FallibleItemDispenserBehavior {
    public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        BlockPos blockPos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
        List<LivingEntity> list = pointer.getWorld().getEntitiesByClass(LivingEntity.class, new Box(blockPos), entity -> entity instanceof CowEntity || entity instanceof GoatEntity);
        ItemStack milkBucket = new ItemStack(Items.MILK_BUCKET);
        if(!list.isEmpty() && !list.get(0).isBaby()) {
            stack.decrement(1);
            if(stack.isEmpty()) {
                return milkBucket;
            } else {
                if(((DispenserBlockEntity) pointer.getBlockEntity()).addToFirstFreeSlot(milkBucket) < 0){
                    super.dispenseSilently(pointer, milkBucket);
                }
            }
            this.setSuccess(true);
        }

        return stack;
    }
}
