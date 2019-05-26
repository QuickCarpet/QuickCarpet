package quickcarpet.feature;

import net.minecraft.block.*;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import quickcarpet.mixin.IMixinDispenserBlock;
import quickcarpet.settings.Settings;

import java.util.Collection;

public class DispenserAddons
{
    public static void registerDefaults()
    {
        for (Identifier identifier : Registry.BLOCK.getIds())
        {
            Block block = Registry.BLOCK.get(identifier);
            Item item = block.asItem();
    
            if (block == null || item == null || !(item instanceof BlockItem))
                continue;
            
            if (!IMixinDispenserBlock.getBehaviour().containsKey(item))
            {
                DispenserBlock.registerBehavior(item, new BehaviourDispenseBlocks((BlockItem)item, block));
            }
        }
    }
    
    public static class BehaviourDispenseBlocks extends ItemDispenserBehavior
    {
        private final ItemDispenserBehavior itemDispenserBehavior = new ItemDispenserBehavior();
        BlockItem blockItem;
        Block block;
        
        public BehaviourDispenseBlocks(BlockItem blockItem, Block block)
        {
            this.block = block;
            this.blockItem = blockItem;
        }
        
        @Override
        public ItemStack dispenseStack(BlockPointer blockPointer, ItemStack itemStack)
        {
            
            if (!Settings.dispensersPlaceBlocks)
            {
                return this.itemDispenserBehavior.dispense(blockPointer, itemStack);
            }
            
            Direction facing = blockPointer.getBlockState().get(DispenserBlock.FACING);
            Direction.Axis axis = facing.getAxis();
            
            BlockPos pos = blockPointer.getBlockPos().offset(facing);
            World world = blockPointer.getWorld();
            
            if (world.isAir(pos) && blockPointer.getBlockState().canPlaceAt(world, pos))
            {
                BlockState state = block.getDefaultState();
                Collection<Property<?>> properties = state.getProperties();
                
                if (properties.contains(FacingBlock.FACING))
                    state = state.with(FacingBlock.FACING, facing);
                else if (properties.contains(HorizontalFacingBlock.FACING) && axis != Direction.Axis.Y)
                    state = state.with(HorizontalFacingBlock.FACING, facing);
                else if (properties.contains(PillarBlock.AXIS))
                    state = state.with(PillarBlock.AXIS, axis);
                
                if (block instanceof StairsBlock)
                    state = state.with(FacingBlock.FACING, facing.getOpposite());
                
                /*
                if (block instanceof LogBlock)
                    state = state.with(LogBlock.AXIS, LogBlock.AXIS.getValue(axis.getName()).orElse());
                */
                
                world.setBlockState(pos, state);
                BlockSoundGroup soundType = state.getSoundGroup();
                world.playSound(null, pos, soundType.getPlaceSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F / 2.0F), soundType.getPitch() * 0.8F);
                itemStack.subtractAmount(1);
                return itemStack;
            }
            
            return super.dispenseStack(blockPointer, itemStack);
        }
    }
}
