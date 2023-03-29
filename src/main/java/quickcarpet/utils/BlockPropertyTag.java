package quickcarpet.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BlockPropertyTag {
    private final TagKey<Block> key;
    private final Predicate<BlockState> property;

    public BlockPropertyTag(Identifier id, BlockPropertyPredicate function) {
        this.key = TagKey.of(RegistryKeys.BLOCK, id);
        this.property = state -> function.test(state, new SingleBlockView(state), BlockPos.ORIGIN);
    }

    public TagKey<Block> getKey() {
        return key;
    }

    public boolean contains(Block block) {
        return this.property.test(block.getDefaultState());
    }

    public List<Block> values() {
        return Registries.BLOCK.stream().filter(this::contains).collect(Collectors.toList());
    }

    @FunctionalInterface
    interface BlockPropertyPredicate {
        boolean test(BlockState state, BlockView world, BlockPos pos);
    }

    public record SingleBlockView(BlockState state) implements BlockView {
        @Nullable
        @Override
        public BlockEntity getBlockEntity(BlockPos pos) {
            return null;
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            return state;
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            return state.getFluidState();
        }

        @Override
        public int getHeight() {
            return 1;
        }

        @Override
        public int getBottomY() {
            return 0;
        }
    }
}
