package quickcarpet.utils;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;
import quickcarpet.feature.CraftingTableBlockEntity;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Supplier;

public class CarpetRegistry {
    public static final BlockEntityType<CraftingTableBlockEntity> CRAFTING_TABLE_BLOCK_ENTITY_TYPE = registerBlockEntity("carpet:crafting_table", CraftingTableBlockEntity::new, Blocks.CRAFTING_TABLE);
    public static void init() {

    }

    private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(String id, Supplier<T> supplier, Block... blocks) {
        try {
            return Registry.register(Registry.BLOCK_ENTITY, id, BlockEntityType.Builder.create(supplier).build(null));
        } catch (NoSuchMethodError e) {
            Method m = Arrays.stream(BlockEntityType.Builder.class.getMethods()).filter(x -> x.getReturnType() == BlockEntityType.Builder.class).findFirst().get();
            try {
                return Registry.register(Registry.BLOCK_ENTITY, id, (BlockEntityType<T>) ((BlockEntityType.Builder) m.invoke(null, supplier, blocks)).build(null));
            } catch (ReflectiveOperationException e1) {
                e1.addSuppressed(e);
                throw new RuntimeException(e1);
            }
        }
    }
}
