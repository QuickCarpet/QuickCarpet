package quickcarpet.utils;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;
import quickcarpet.feature.CraftingTableBlockEntity;

public class CarpetRegistry {
    public static final BlockEntityType<CraftingTableBlockEntity> CRAFTING_TABLE_BLOCK_ENTITY_TYPE = Registry.register(Registry.BLOCK_ENTITY, "carpet:crafting_table", BlockEntityType.Builder.create(CraftingTableBlockEntity::new).build(null));
    public static void init() {

    }
}
