package quickcarpet.utils;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import quickcarpet.feature.CraftingTableBlockEntity;
import quickcarpet.feature.PlaceBlockDispenserBehavior;
import quickcarpet.mixin.BlockTagsAccessor;

import java.util.List;
import java.util.function.Supplier;

public class CarpetRegistry {
    // Initializes Reflection
    public static final BlockEntityType<CraftingTableBlockEntity> CRAFTING_TABLE_BLOCK_ENTITY_TYPE = registerBlockEntity("carpet:crafting_table", CraftingTableBlockEntity::new, Blocks.CRAFTING_TABLE);
    static { BlockTags.getContainer(); } // load BlockTags class
    public static final Tag<Block> SIMPLE_FULL_BLOCK = new BlockPropertyTag(new Identifier("carpet:simple_full_block"), BlockState::isSimpleFullBlock);
    public static final Tag<Block> FULL_CUBE = new BlockPropertyTag(new Identifier("carpet:full_cube"), (state, world, pos) -> Block.isShapeFullCube(state.getCollisionShape(world, pos)));
    public static final List<Tag<Block>> VIRTUAL_BLOCK_TAGS = ImmutableList.of(SIMPLE_FULL_BLOCK, FULL_CUBE);

    public static final Tag<Block> DISPENSER_PLACEABLE = BlockTagsAccessor.register("carpet:dispenser_placeable");
    public static final DispenserBehavior PLACE_BLOCK_DISPENSER_BEHAVIOR = new PlaceBlockDispenserBehavior();

    private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(String id, Supplier<T> supplier, Block... blocks) {
        return Registry.register(Registry.BLOCK_ENTITY, id, Reflection.newBlockEntityTypeBuilder(supplier, blocks).build(null));
    }

    public static void init() {
        // initializes statics of CarpetRegistry
    }
}
