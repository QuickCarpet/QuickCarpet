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
import quickcarpet.feature.BreakBlockDispenserBehavior;
import quickcarpet.feature.CraftingTableBlockEntity;
import quickcarpet.feature.PlaceBlockDispenserBehavior;
import quickcarpet.feature.TillSoilDispenserBehavior;
import quickcarpet.mixin.accessor.BlockTagsAccessor;

import java.util.List;
import java.util.function.Supplier;

public class CarpetRegistry {
    // Initializes Reflection
    public static final BlockEntityType<CraftingTableBlockEntity> CRAFTING_TABLE_BLOCK_ENTITY_TYPE = registerBlockEntity("carpet:crafting_table", CraftingTableBlockEntity::new, Blocks.CRAFTING_TABLE);

    static { BlockTags.getContainer(); } // load BlockTags class
    public static final Tag<Block> SIMPLE_FULL_BLOCK = new BlockPropertyTag(new Identifier("carpet:simple_full_block"), BlockState::isSimpleFullBlock);
    public static final Tag<Block> FULL_CUBE = new BlockPropertyTag(new Identifier("carpet:full_cube"), (state, world, pos) -> Block.isShapeFullCube(state.getCollisionShape(world, pos)));
    public static final List<Tag<Block>> VIRTUAL_BLOCK_TAGS = ImmutableList.of(SIMPLE_FULL_BLOCK, FULL_CUBE);

    public static final Tag<Block> DISPENSER_BLOCK_WHITELIST = BlockTagsAccessor.register("carpet:dispenser_placeable_whitelist");
    public static final Tag<Block> DISPENSER_BLOCK_BLACKLIST = BlockTagsAccessor.register("carpet:dispenser_placeable_blacklist");
    public static final DispenserBehavior PLACE_BLOCK_DISPENSER_BEHAVIOR = new PlaceBlockDispenserBehavior();
    public static final DispenserBehavior BREAK_BLOCK_DISPENSER_BEHAVIOR = new BreakBlockDispenserBehavior();
    public static final DispenserBehavior DISPENSERS_TILL_SOIL_BEHAVIOR = new TillSoilDispenserBehavior();

    //Additional Movable Blocks
    public static final Tag<Block> PISTON_OVERRIDE_MOVABLE = BlockTagsAccessor.register("carpet:piston_movable");
    public static final Tag<Block> PISTON_OVERRIDE_PUSH_ONLY = BlockTagsAccessor.register("carpet:piston_push_only");
    public static final Tag<Block> PISTON_OVERRIDE_IMMOVABLE = BlockTagsAccessor.register("carpet:piston_immovable");
    public static final Tag<Block> PISTON_OVERRIDE_DESTROY = BlockTagsAccessor.register("carpet:piston_destroy");
    public static final Tag<Block> PISTON_OVERRIDE_WEAK_STICKY = BlockTagsAccessor.register("carpet:piston_weak_sticky");


    private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(String id, Supplier<T> supplier, Block... blocks) {
        return Registry.register(Registry.BLOCK_ENTITY_TYPE, id, Reflection.newBlockEntityTypeBuilder(supplier, blocks).build(null));
    }

    public static void init() {
        // initializes statics of CarpetRegistry
    }

    public static boolean isIgnoredForSync(Identifier key) {
        return key.getNamespace().equals("carpet");
    }
}
