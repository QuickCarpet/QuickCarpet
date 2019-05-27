package quickcarpet.feature;

import net.minecraft.block.*;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import quickcarpet.settings.Settings;

import java.util.Collection;

public class PlaceBlockDispenserBehavior  extends ItemDispenserBehavior {
    @Override
    public ItemStack dispenseStack(BlockPointer blockPointer, ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (!Settings.dispensersPlaceBlocks || !(item instanceof BlockItem)) {
            return super.dispenseStack(blockPointer, itemStack);
        }
        Block block = ((BlockItem) item).getBlock();

        Direction facing = blockPointer.getBlockState().get(DispenserBlock.FACING);
        Direction.Axis axis = facing.getAxis();

        BlockPos pos = blockPointer.getBlockPos().offset(facing);
        World world = blockPointer.getWorld();
        BlockState state = block.getDefaultState();
        Collection<Property<?>> properties = state.getProperties();

        if (properties.contains(FacingBlock.FACING)) {
            state = state.with(FacingBlock.FACING, facing);
        } else if (properties.contains(HorizontalFacingBlock.FACING) && axis != Direction.Axis.Y) {
            state = state.with(HorizontalFacingBlock.FACING, facing);
        } else if (properties.contains(PillarBlock.AXIS)) {
            state = state.with(PillarBlock.AXIS, axis);
        }

        if (block instanceof StairsBlock) {
            state = state.with(FacingBlock.FACING, facing.getOpposite());
        }

        if (world.isAir(pos) && state.canPlaceAt(world, pos)) {
            world.setBlockState(pos, state);
            BlockSoundGroup soundType = state.getSoundGroup();
            world.playSound(null, pos, soundType.getPlaceSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F / 2.0F), soundType.getPitch() * 0.8F);
            itemStack.subtractAmount(1);
            return itemStack;
        }

        return super.dispenseStack(blockPointer, itemStack);
    }
}
