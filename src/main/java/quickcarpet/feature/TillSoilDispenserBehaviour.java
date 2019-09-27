package quickcarpet.feature;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import quickcarpet.settings.Settings;

import java.util.Random;

public class TillSoilDispenserBehaviour extends ItemDispenserBehavior
{
    @Override
    protected ItemStack dispenseSilently(BlockPointer blockPointer_1, ItemStack itemStack_1)
    {
        if (!Settings.dispensersTillSoil)
            return super.dispenseSilently(blockPointer_1, itemStack_1);
    
        World world = blockPointer_1.getWorld();
        Direction direction = blockPointer_1.getBlockState().get(DispenserBlock.FACING);
        BlockPos front = blockPointer_1.getBlockPos().offset(direction);
        BlockPos down = blockPointer_1.getBlockPos().method_10074().offset(direction); // method_10074 = down
        BlockState frontState = world.getBlockState(front);
        BlockState downState = world.getBlockState(down);
        
        if (isFarmland(frontState) || isFarmland(downState))
            return itemStack_1;
        
        if (canDirectlyTurnToFarmland(frontState))
            world.setBlockState(front, Blocks.FARMLAND.getDefaultState());
        else if (canDirectlyTurnToFarmland(downState))
            world.setBlockState(down, Blocks.FARMLAND.getDefaultState());
        else if (frontState.getBlock() == Blocks.COARSE_DIRT)
            world.setBlockState(front, Blocks.DIRT.getDefaultState());
        else if (downState.getBlock() == Blocks.COARSE_DIRT)
            world.setBlockState(down, Blocks.DIRT.getDefaultState());
            
        
        if (itemStack_1.damage(1, (Random)world.random, (ServerPlayerEntity)null))
            itemStack_1.setCount(0);
        
        return itemStack_1;
    }
    
    private boolean canDirectlyTurnToFarmland(BlockState state)
    {
        return state.getBlock() == Blocks.DIRT || state.getBlock() == Blocks.GRASS_BLOCK || state.getBlock() == Blocks.GRASS_PATH;
    }
    
    private boolean isFarmland(BlockState state)
    {
        return state.getBlock() == Blocks.FARMLAND;
    }
}
