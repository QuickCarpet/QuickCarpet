package quickcarpet.utils;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.potion.Potion;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.TradeOffers;
import quickcarpet.feature.CraftingTableBlockEntity;

import java.util.function.Supplier;

public class CarpetRegistry {
    // Initializes Reflection
    public static final BlockEntityType<CraftingTableBlockEntity> CRAFTING_TABLE_BLOCK_ENTITY_TYPE = registerBlockEntity("carpet:crafting_table", CraftingTableBlockEntity::new, Blocks.CRAFTING_TABLE);
    
    public static Potion SUPER_LONG_NIGHT_VISION;
    public static Potion SUPER_LONG_INVISIBILITY;
    public static Potion SUPER_LONG_LEAPING;
    public static Potion SUPER_STRONG_LEAPING;
    public static Potion SUPER_LONG_FIRE_RESISTANCE;
    public static Potion SUPER_LONG_SWIFTNESS;
    public static Potion SUPER_STRONG_SWIFTNESS;
    public static Potion SUPER_LONG_SLOWNESS;
    public static Potion SUPER_STRONG_SLOWNESS;
    public static Potion SUPER_LONG_TURTLE_MASTER;
    public static Potion SUPER_STRONG_TURTLE_MASTER;
    public static Potion SUPER_LONG_WATER_BREATHING;
    public static Potion SUPER_LONG_POISON;
    public static Potion SUPER_STRONG_POISON;
    public static Potion SUPER_LONG_REGENERATION;
    public static Potion SUPER_STRONG_REGENERATION;
    public static Potion SUPER_LONG_STRENGTH;
    public static Potion SUPER_STRONG_STRENGTH;
    public static Potion SUPER_LONG_WEAKNESS;
    public static Potion SUPER_LONG_SLOW_FALLING;

    private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(String id, Supplier<T> supplier, Block... blocks) {
        return Registry.register(Registry.BLOCK_ENTITY, id, Reflection.newBlockEntityTypeBuilder(supplier, blocks).build(null));
    }

    public static void init() {
        // initializes statics of CarpetRegistry
    }
}
