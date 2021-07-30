package quickcarpet.feature.dispenser;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Saddleable;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;

public class SmartSaddleBehavior extends FallibleItemDispenserBehavior {
    public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        BlockPos blockPos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
        List<LivingEntity> list = pointer.getWorld().getEntitiesByClass(LivingEntity.class, new Box(blockPos), entity -> entity instanceof Saddleable saddleable && !saddleable.isSaddled() && saddleable.canBeSaddled());
        if (!list.isEmpty()) {
            ((Saddleable)list.get(0)).saddle(SoundCategory.BLOCKS);
            stack.decrement(1);
            this.setSuccess(true);
        } else {
            this.setSuccess(false);
        }
        return stack;
    }
}