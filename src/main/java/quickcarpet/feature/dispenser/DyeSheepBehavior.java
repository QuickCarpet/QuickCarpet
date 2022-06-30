package quickcarpet.feature.dispenser;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;

public class DyeSheepBehavior extends FallibleItemDispenserBehavior {
    public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        BlockPos blockPos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
        List<LivingEntity> list = pointer.getWorld().getEntitiesByClass(LivingEntity.class, new Box(blockPos), entity -> entity instanceof SheepEntity);
        if(stack.getItem() instanceof DyeItem dyeItem) {
            if (!list.isEmpty() && !((SheepEntity) list.get(0)).isSheared() && ((SheepEntity) list.get(0)).getColor() != dyeItem.getColor()) {
                stack.decrement(1);
                ((SheepEntity) list.get(0)).setColor(dyeItem.getColor());
                this.setSuccess(true);
            }
        }
        return stack;
    }
}
