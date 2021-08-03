package quickcarpet.feature.dispenser;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PickupBucketablesBehavior extends FallibleItemDispenserBehavior {
    public ItemStack dispenseSilently(BlockPointer blockPointer, ItemStack stack) {
        ServerWorld world = blockPointer.getWorld();
        Direction facing = blockPointer.getBlockState().get(DispenserBlock.FACING);
        BlockPos target = blockPointer.getPos().offset(facing);
        BlockState state = world.getBlockState(target);
        Item item = stack.getItem();

        if (state.getBlock() == Blocks.WATER && item == Items.BUCKET) {
            world.setBlockState(target, Blocks.AIR.getDefaultState());
            this.setSuccess(true);
            return getBucketableAnimals(world, target);
        }
        this.setSuccess(false);
        return stack;
    }

    @Nullable
    private ItemStack getBucketableAnimals(World world, BlockPos pos) {
        List<Entity> list = world.getEntitiesByClass(Entity.class, new Box(pos.getX(), pos.getY(), pos.getZ(), (pos.getX() + 1), (pos.getY() + 1), (pos.getZ() + 1)), (bucketable) -> bucketable instanceof Bucketable);
        if (list.size() >= 1) {
            Entity first = list.get(0);
            first.remove(Entity.RemovalReason.DISCARDED);

            ItemStack stack = ((Bucketable) first).getBucketItem();

            if(first instanceof AxolotlEntity){
                Bucketable.copyDataToStack((MobEntity) first, stack);
                NbtCompound nbtCompound = stack.getOrCreateTag();
                nbtCompound.putInt("Variant", ((AxolotlEntity) first).getVariant().getId());
                nbtCompound.putInt("Age", ((AxolotlEntity) first).getBreedingAge());
                Brain<?> brain = ((AxolotlEntity) first).getBrain();
                if (brain.hasMemoryModule(MemoryModuleType.HAS_HUNTING_COOLDOWN)) {
                    nbtCompound.putLong("HuntingCooldown", brain.getMemory(MemoryModuleType.HAS_HUNTING_COOLDOWN));
                }
            } else if(first instanceof TropicalFishEntity) {
                Bucketable.copyDataToStack((MobEntity) first, stack);
                NbtCompound nbtCompound = stack.getOrCreateTag();
                nbtCompound.putInt("BucketVariantTag", ((TropicalFishEntity) first).getVariant());
            } else {
                Bucketable.copyDataToStack((MobEntity) first, stack);
            }

            return stack;
        } else {
            return new ItemStack(Items.WATER_BUCKET);
        }
    }
}
