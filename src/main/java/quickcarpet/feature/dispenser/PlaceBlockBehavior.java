package quickcarpet.feature.dispenser;

import net.minecraft.block.*;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import quickcarpet.QuickCarpet;
import quickcarpet.settings.Settings;
import quickcarpet.utils.CarpetRegistry;

import java.util.Collection;

public class PlaceBlockBehavior extends FallibleItemDispenserBehavior {
    @Override
    public ItemStack dispenseSilently(BlockPointer blockPointer, ItemStack itemStack) {
        setSuccess(false);
        Item item = itemStack.getItem();
        if (Settings.dispensersPlaceBlocks == Option.FALSE || !(item instanceof BlockItem)) {
            return itemStack;
        }
        Block block = ((BlockItem) item).getBlock();

        Direction facing = blockPointer.getBlockState().get(DispenserBlock.FACING);
        Direction.Axis axis = facing.getAxis();
        World world = blockPointer.getWorld();
        BlockPos pos = blockPointer.getPos();

        final Direction ffacing = facing;

        if (usePlacementContext(item, block)) {
            BlockHitResult hitResult = new BlockHitResult(Vec3d.ofCenter(pos.offset(facing, 2)), facing, pos, false);
            ItemPlacementContext ipc = new ItemPlacementContext(world, null, Hand.MAIN_HAND, itemStack, hitResult) {
                @Override
                public Direction getPlayerLookDirection() {
                    return ffacing;
                }

                @Override
                public Direction getVerticalPlayerLookDirection() {
                    return ffacing.getAxis() != Direction.Axis.Y ? Direction.UP : ffacing;
                }

                @Override
                public Direction[] getPlacementDirections() {
                    return new Direction[] {getPlayerLookDirection(), Direction.UP, Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
                }
            };
            ActionResult result = ((BlockItem) item).place(ipc);
            setSuccess(result.isAccepted());
            return itemStack;
        }

        pos = pos.offset(facing);

        BlockState state = block.getDefaultState();
        if (state == null) return itemStack;
        Collection<Property<?>> properties = state.getProperties();

        if (block instanceof StairsBlock) {
            facing = facing.getOpposite();
        }

        if (properties.contains(Properties.FACING)) {
            state = state.with(Properties.FACING, facing);
        } else if (properties.contains(Properties.HORIZONTAL_FACING) && axis != Direction.Axis.Y) {
            state = state.with(Properties.HORIZONTAL_FACING, facing);
        } else if (properties.contains(Properties.HOPPER_FACING) && axis != Direction.Axis.Y) {
            state = state.with(Properties.HOPPER_FACING, facing);
        } else if (properties.contains(Properties.AXIS)) {
            state = state.with(Properties.AXIS, axis);
        } else if (properties.contains(Properties.HORIZONTAL_AXIS)  && axis != Direction.Axis.Y) {
            state = state.with(Properties.HORIZONTAL_AXIS, axis);
        }

        if (properties.contains(Properties.BLOCK_HALF)) {
            state = state.with(Properties.BLOCK_HALF, facing == Direction.UP ? BlockHalf.TOP : BlockHalf.BOTTOM);
        } else if (properties.contains(Properties.SLAB_TYPE)) {
            state = state.with(Properties.SLAB_TYPE, facing == Direction.DOWN ? SlabType.TOP : SlabType.BOTTOM);
        }

        if (properties.contains(Properties.WATERLOGGED)) {
            state = state.with(Properties.WATERLOGGED, false);
        }

        if (block instanceof ObserverBlock) {
            state = state.with(ObserverBlock.POWERED, true);
        }

        state = Block.postProcessState(state, world, pos);

        BlockState currentBlockState = world.getBlockState(pos);
        FluidState currentFluidState = world.getFluidState(pos);
        if ((world.isAir(pos) || currentBlockState.getMaterial().isReplaceable()) && currentBlockState.getBlock() != block && state.canPlaceAt(world, pos)) {
            if (currentFluidState.isStill() && block instanceof FluidFillable) {
                if (!((FluidFillable) block).tryFillWithFluid(world, pos, state, currentFluidState)) {
                    world.setBlockState(pos, state);
                }
            } else {
                world.setBlockState(pos, state);
            }
            NbtCompound blockEntityTag = itemStack.getSubNbt("BlockEntityTag");
            if (blockEntityTag != null && block instanceof BlockEntityProvider) {
                BlockEntity be = world.getBlockEntity(pos);
                if (be != null) {
                    blockEntityTag = new NbtCompound().copyFrom(blockEntityTag);
                    blockEntityTag.putInt("x", pos.getX());
                    blockEntityTag.putInt("y", pos.getY());
                    blockEntityTag.putInt("z", pos.getZ());
                    be.readNbt(blockEntityTag);
                } else {
                    QuickCarpet.LOGGER.warn("Expected a BlockEntity for {} at {},{},{}", state, pos.getX(), pos.getY(), pos.getZ());
                }
            }
            BlockSoundGroup soundType = state.getSoundGroup();
            world.playSound(null, pos, soundType.getPlaceSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F / 2.0F), soundType.getPitch() * 0.8F);
            if (!world.isAir(pos)) {
                itemStack.decrement(1);
                setSuccess(true);
                return itemStack;
            }
        }

        return itemStack;
    }

    public enum Option {
        FALSE, WHITELIST, BLACKLIST, ALL
    }

    public static boolean canPlace(BlockState block) {
        return switch (Settings.dispensersPlaceBlocks) {
            case WHITELIST -> block.isIn(CarpetRegistry.DISPENSER_BLOCK_WHITELIST);
            case BLACKLIST -> !block.isIn(CarpetRegistry.DISPENSER_BLOCK_BLACKLIST);
            case ALL -> true;
            default -> false;
        };
    }

    private static boolean usePlacementContext(Item item, Block block) {
        return item.getClass() != BlockItem.class || block instanceof SeaPickleBlock || block instanceof TurtleEggBlock;
    }
}
