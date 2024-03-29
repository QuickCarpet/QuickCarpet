package quickcarpet.feature.dispenser;

import com.google.common.collect.BiMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.item.HoneycombItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldEvents;

import java.util.Optional;

public class ScrapeCopperBehavior extends FallibleItemDispenserBehavior {
    @Override
    protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        ServerWorld world = pointer.getWorld();
        Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);
        BlockPos front = pointer.getPos().offset(direction);
        BlockState state = world.getBlockState(front);
        Optional<BlockState> decreasedOxidationState = Oxidizable.getDecreasedOxidationState(state);
        Optional<BlockState> unwaxedState = Optional.ofNullable(HoneycombItem.WAXED_TO_UNWAXED_BLOCKS.get().get(state.getBlock())).map(block -> block.getStateWithProperties(state));
        Optional<BlockState> convertedState;
        if (decreasedOxidationState.isPresent()) {
            world.playSound(null, front, SoundEvents.ITEM_AXE_SCRAPE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.syncWorldEvent(null, WorldEvents.BLOCK_SCRAPED, front, 0);
            convertedState = decreasedOxidationState;
        } else if (unwaxedState.isPresent()) {
            world.playSound(null, front, SoundEvents.ITEM_AXE_WAX_OFF, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.syncWorldEvent(null, WorldEvents.WAX_REMOVED, front, 0);
            convertedState = unwaxedState;
        } else {
            this.setSuccess(false);
            return stack;
        }
        if (stack.damage(1, world.random, null)) stack.setCount(0);
        world.setBlockState(front, convertedState.get());
        this.setSuccess(true);
        return stack;
    }
}
