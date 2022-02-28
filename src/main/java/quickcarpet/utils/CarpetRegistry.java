package quickcarpet.utils;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.datafixer.Schemas;
import net.minecraft.entity.EntityType;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.StructureSpawns;
import net.minecraft.world.biome.SpawnSettings;
import quickcarpet.feature.CraftingTableBlockEntity;

import java.util.List;

public class CarpetRegistry {
    private static final Schema SCHEMA = Schemas.getFixer().getSchema(DataFixUtils.makeKey(SharedConstants.getGameVersion().getWorldVersion()));
    public static final BlockEntityType<CraftingTableBlockEntity> CRAFTING_TABLE_BLOCK_ENTITY_TYPE = registerBlockEntity("carpet:crafting_table", CraftingTableBlockEntity::new, CraftingTableBlockEntity.getType(SCHEMA), Blocks.CRAFTING_TABLE);

    public static final BlockPropertyTag SIMPLE_FULL_BLOCK = new BlockPropertyTag(new Identifier("carpet:simple_full_block"), BlockState::isOpaqueFullCube);
    public static final BlockPropertyTag FULL_CUBE = new BlockPropertyTag(new Identifier("carpet:full_cube"), BlockState::isFullCube);
    public static final List<BlockPropertyTag> VIRTUAL_BLOCK_TAGS = List.of(SIMPLE_FULL_BLOCK, FULL_CUBE);

    public static final TagKey<Block> DISPENSER_BLOCK_WHITELIST = TagKey.of(Registry.BLOCK_KEY, new Identifier("carpet:dispenser_placeable_whitelist"));
    public static final TagKey<Block> DISPENSER_BLOCK_BLACKLIST = TagKey.of(Registry.BLOCK_KEY, new Identifier("carpet:dispenser_placeable_blacklist"));

    //Additional Movable Blocks
    public static final TagKey<Block> PISTON_OVERRIDE_MOVABLE = TagKey.of(Registry.BLOCK_KEY, new Identifier("carpet:piston_movable"));
    public static final TagKey<Block> PISTON_OVERRIDE_PUSH_ONLY = TagKey.of(Registry.BLOCK_KEY, new Identifier("carpet:piston_push_only"));
    public static final TagKey<Block> PISTON_OVERRIDE_IMMOVABLE = TagKey.of(Registry.BLOCK_KEY, new Identifier("carpet:piston_immovable"));
    public static final TagKey<Block> PISTON_OVERRIDE_DESTROY = TagKey.of(Registry.BLOCK_KEY, new Identifier("carpet:piston_destroy"));
    public static final TagKey<Block> PISTON_OVERRIDE_WEAK_STICKY = TagKey.of(Registry.BLOCK_KEY, new Identifier("carpet:piston_weak_sticky"));

    public static final Object2IntMap<Block> TERRACOTTA_BLOCKS = new Object2IntOpenHashMap<>();

    static {
        TERRACOTTA_BLOCKS.put(Blocks.WHITE_TERRACOTTA, 0);
        TERRACOTTA_BLOCKS.put(Blocks.ORANGE_TERRACOTTA, 1);
        TERRACOTTA_BLOCKS.put(Blocks.MAGENTA_TERRACOTTA, 2);
        TERRACOTTA_BLOCKS.put(Blocks.LIGHT_BLUE_TERRACOTTA, 3);
        TERRACOTTA_BLOCKS.put(Blocks.YELLOW_TERRACOTTA, 4);
        TERRACOTTA_BLOCKS.put(Blocks.LIME_TERRACOTTA, 5);
        TERRACOTTA_BLOCKS.put(Blocks.PINK_TERRACOTTA, 6);
        TERRACOTTA_BLOCKS.put(Blocks.GRAY_TERRACOTTA, 7);
        TERRACOTTA_BLOCKS.put(Blocks.LIGHT_GRAY_TERRACOTTA, 8);
        TERRACOTTA_BLOCKS.put(Blocks.CYAN_TERRACOTTA, 9);
        TERRACOTTA_BLOCKS.put(Blocks.PURPLE_TERRACOTTA, 10);
        TERRACOTTA_BLOCKS.put(Blocks.BLUE_TERRACOTTA, 11);
        TERRACOTTA_BLOCKS.put(Blocks.BROWN_TERRACOTTA, 12);
        TERRACOTTA_BLOCKS.put(Blocks.GREEN_TERRACOTTA, 13);
        TERRACOTTA_BLOCKS.put(Blocks.RED_TERRACOTTA, 14);
        TERRACOTTA_BLOCKS.put(Blocks.BLACK_TERRACOTTA, 15);
    }

    public static final Pool<SpawnSettings.SpawnEntry> END_CITY_SPAWN_POOL = Pool.of(new SpawnSettings.SpawnEntry(EntityType.SHULKER, 10, 4, 4));
    public static final StructureSpawns END_CITY_SPAWNS = new StructureSpawns(StructureSpawns.BoundingBox.PIECE, END_CITY_SPAWN_POOL);

    private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(String id, BlockEntityType.BlockEntityFactory<? extends T> supplier, Type<?> type, Block... blocks) {
        return Registry.register(Registry.BLOCK_ENTITY_TYPE, id, new BlockEntityType<>(supplier, ImmutableSet.copyOf(blocks), type));
    }

    public static void init() {
        // initializes statics of CarpetRegistry
    }

    public static boolean isIgnoredForSync(Identifier key) {
        return key.getNamespace().equals("carpet");
    }


}
