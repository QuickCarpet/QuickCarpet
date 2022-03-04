package quickcarpet.feature.dispenser;

import net.minecraft.block.*;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quickcarpet.settings.Settings;

import java.util.List;

public class InteractCauldronBehavior extends FallibleItemDispenserBehavior {
    @Nullable
    private static ItemStack getBucketableAnimals(World world, BlockPos pos) {
        List<Entity> list = world.getEntitiesByClass(Entity.class, new Box(pos.getX(), pos.getY(), pos.getZ(), (pos.getX() + 1), (pos.getY() + 1), (pos.getZ() + 1)), (bucketable) -> bucketable instanceof Bucketable);
        if (list.size() >= 1) {
            list.get(0).remove(Entity.RemovalReason.DISCARDED);
            return ((Bucketable)list.get(0)).getBucketItem();
        } else {
            return new ItemStack(Items.WATER_BUCKET);
        }
    }

    public ItemStack dispenseSilently(BlockPointer blockPointer, ItemStack stack) {
        ServerWorld world = blockPointer.getWorld();
        Direction facing = blockPointer.getBlockState().get(DispenserBlock.FACING);
        BlockPos target = blockPointer.getPos().offset(facing);
        BlockState state = world.getBlockState(target);
        Item item = stack.getItem();
        if (item != Items.BUCKET) {
            if (item == Items.POTION) {
                //If the potion isn't a Water Bottle, fail
                if (PotionUtil.getPotion(stack) != Potions.WATER) {
                    this.setSuccess(false);
                    return stack;
                }
                return handleWaterPotion(stack, world, target, state);
            } else if (item == Items.GLASS_BOTTLE) {
                if (state.getBlock() == Blocks.WATER_CAULDRON) {
                    return handleGlassBottle(blockPointer, stack, world, target, state);
                } else {
                    this.setSuccess(false);
                    return stack;
                }
            } else {
                return handleFilledBucket(blockPointer, stack, world, target, state, item);
            }
        } else if (item instanceof EntityBucketItem entityBucketItem && state.getBlock() == Blocks.WATER_CAULDRON) { //Dispense entity into water cauldron
            return handleEntityBucket(blockPointer, stack, world, target, entityBucketItem);
        }
        return handleEmptyBucket(blockPointer, stack, world, target, state);
    }

    private ItemStack handleGlassBottle(BlockPointer blockPointer, ItemStack stack, ServerWorld world, BlockPos target, BlockState state) {
        LeveledCauldronBlock.decrementFluidLevel(state, world, target);
        stack.decrement(1);
        this.setSuccess(true);
        if(stack.isEmpty()){
            return PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.WATER);
        } else {
            if (((DispenserBlockEntity) blockPointer.getBlockEntity()).addToFirstFreeSlot(new ItemStack(Items.POTION)) < 0) {
                super.dispenseSilently(blockPointer, PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.WATER));
            }
        }
        return stack;
    }

    private ItemStack handleWaterPotion(ItemStack stack, ServerWorld world, BlockPos target, BlockState state) {
        if (state.getBlock() == Blocks.CAULDRON) {
            world.setBlockState(target, Blocks.WATER_CAULDRON.getDefaultState());
            stack.decrement(1);
            this.setSuccess(true);
            return new ItemStack(Items.GLASS_BOTTLE);
        }
        if (state.getBlock() == Blocks.WATER_CAULDRON && state.get(LeveledCauldronBlock.LEVEL) != 3) {
            world.setBlockState(target, (BlockState) state.cycle(LeveledCauldronBlock.LEVEL));
            stack.decrement(1);
            this.setSuccess(true);
            return new ItemStack(Items.GLASS_BOTTLE);
        }
        this.setSuccess(false);
        return stack;
    }

    @NotNull
    private ItemStack handleEntityBucket(BlockPointer blockPointer, ItemStack stack, ServerWorld world, BlockPos target, EntityBucketItem entityBucketItem) {
        entityBucketItem.onEmptied(null, world, stack, target);
        stack.decrement(1);
        if (stack.isEmpty()) {
            this.setSuccess(true);
            return new ItemStack(Items.BUCKET);
        } else {

            if (((DispenserBlockEntity) blockPointer.getBlockEntity()).addToFirstFreeSlot(new ItemStack(Items.BUCKET)) < 0) {
                new ItemDispenserBehavior().dispense(blockPointer, new ItemStack(Items.BUCKET));
            }
            this.setSuccess(true);
            return stack;
        }
    }

    private ItemStack handleEmptyBucket(BlockPointer blockPointer, ItemStack stack, ServerWorld world, BlockPos target, BlockState state) {
        boolean isFull;
        ItemStack bucketType = null;
        Block block = state.getBlock();
        if (block == Blocks.WATER_CAULDRON) {
            isFull = ((LeveledCauldronBlock) state.getBlock()).isFull(state);
            if (Settings.dispensersPickupBucketables) {
                bucketType = getBucketableAnimals(world, target);
            }
            if (bucketType == null) {
                bucketType = new ItemStack(Items.WATER_BUCKET);
            }
        } else if (block == Blocks.LAVA_CAULDRON) {
            isFull = ((LavaCauldronBlock) state.getBlock()).isFull(state);
            bucketType = new ItemStack(Items.LAVA_BUCKET);
        } else if (block == Blocks.POWDER_SNOW_CAULDRON) {
            isFull = ((PowderSnowCauldronBlock) state.getBlock()).isFull(state);
            bucketType = new ItemStack(Items.POWDER_SNOW_BUCKET);
        } else {
            this.setSuccess(false);
            return stack;
        }
        if (isFull) {
            world.setBlockState(target, Blocks.CAULDRON.getDefaultState());
            stack.decrement(1);
            if (stack.isEmpty()) {
                this.setSuccess(true);
                return bucketType;
            }
            if (((DispenserBlockEntity) blockPointer.getBlockEntity()).addToFirstFreeSlot(bucketType) < 0) {
                super.dispenseSilently(blockPointer, bucketType);
            }
            this.setSuccess(true);
        } else {
            this.setSuccess(false);
        }
        return stack;
    }

    private ItemStack handleFilledBucket(BlockPointer blockPointer, ItemStack stack, ServerWorld world, BlockPos target, BlockState state, Item item) {
        if (state.getBlock() == Blocks.CAULDRON) {

            if (item == Items.LAVA_BUCKET) {
                world.setBlockState(target, Blocks.LAVA_CAULDRON.getDefaultState());
            } else if (item == Items.WATER_BUCKET) {
                world.setBlockState(target, Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3));
            } else if (item == Items.POWDER_SNOW_BUCKET) {
                world.setBlockState(target, Blocks.POWDER_SNOW_CAULDRON.getDefaultState().with(PowderSnowCauldronBlock.LEVEL, 3));
            } else if (item instanceof EntityBucketItem) {
                world.setBlockState(target, Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3));
            }

            if (item instanceof BucketItem bucket) bucket.onEmptied(null, world, stack, target);

            stack.decrement(1);
            if (stack.isEmpty()) {
                this.setSuccess(true);
                return new ItemStack(Items.BUCKET);
            }
            if (((DispenserBlockEntity) blockPointer.getBlockEntity()).addToFirstFreeSlot(new ItemStack(Items.BUCKET)) < 0) {
                new ItemDispenserBehavior().dispense(blockPointer, new ItemStack(Items.BUCKET));
            }
            this.setSuccess(true);
        } else {
            this.setSuccess(false);
        }
        return stack;
    }
}
