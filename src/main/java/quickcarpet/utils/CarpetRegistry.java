package quickcarpet.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.SharedConstants;
import net.minecraft.block.*;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.datafixer.Schemas;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.*;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import quickcarpet.feature.*;
import quickcarpet.mixin.accessor.BlockTagsAccessor;
import quickcarpet.settings.Settings;

import java.util.List;
import java.util.Map;

public class CarpetRegistry {
    private static final Schema SCHEMA = Schemas.getFixer().getSchema(DataFixUtils.makeKey(SharedConstants.getGameVersion().getWorldVersion()));
    public static final BlockEntityType<CraftingTableBlockEntity> CRAFTING_TABLE_BLOCK_ENTITY_TYPE = registerBlockEntity("carpet:crafting_table", CraftingTableBlockEntity::new, CraftingTableBlockEntity.getType(SCHEMA), Blocks.CRAFTING_TABLE);

    static { BlockTags.getTagGroup(); } // load BlockTags class
    public static final BlockPropertyTag SIMPLE_FULL_BLOCK = new BlockPropertyTag(new Identifier("carpet:simple_full_block"), BlockState::isOpaqueFullCube);
    public static final BlockPropertyTag FULL_CUBE = new BlockPropertyTag(new Identifier("carpet:full_cube"), BlockState::isFullCube);
    public static final List<BlockPropertyTag> VIRTUAL_BLOCK_TAGS = ImmutableList.of(SIMPLE_FULL_BLOCK, FULL_CUBE);

    public static final Tag.Identified<Block> DISPENSER_BLOCK_WHITELIST = BlockTagsAccessor.register("carpet:dispenser_placeable_whitelist");
    public static final Tag.Identified<Block> DISPENSER_BLOCK_BLACKLIST = BlockTagsAccessor.register("carpet:dispenser_placeable_blacklist");
    public static final DispenserBehavior PLACE_BLOCK_DISPENSER_BEHAVIOR = new PlaceBlockDispenserBehavior();
    public static final DispenserBehavior BREAK_BLOCK_DISPENSER_BEHAVIOR = new BreakBlockDispenserBehavior();
    public static final DispenserBehavior DISPENSERS_TILL_SOIL_BEHAVIOR = new TillSoilDispenserBehavior();
    public static final DispenserBehavior DISPENSERS_STRIP_LOGS_BEHAVIOR = new StripLogsDispenserBehavior();
    public static final DispenserBehavior DISPENSER_INTERACT_CAULDRON = new InteractCauldronDispenserBehavior();
    public static final DispenserBehavior SMART_SADDLE_DISPENSER_BEHAVIOR = new FallibleItemDispenserBehavior() {
        public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            BlockPos blockPos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
            List<LivingEntity> list = pointer.getWorld().getEntitiesByClass(LivingEntity.class, new Box(blockPos), entity -> entity instanceof Saddleable saddleable && !saddleable.isSaddled() && saddleable.canBeSaddled());
            if (!list.isEmpty()) {
                ((Saddleable)list.get(0)).saddle(SoundCategory.BLOCKS);
                stack.decrement(1);
                this.setSuccess(true);
            } else {
                this.setSuccess(false);
            }
            return stack;
        }
    };

    public static final DispenserBehavior DISPENSER_PICKUP_BUCKETABLES = new FallibleItemDispenserBehavior() {
        public ItemStack dispenseSilently(BlockPointer blockPointer, ItemStack stack) {
            ServerWorld world = blockPointer.getWorld();
            Direction facing = blockPointer.getBlockState().get(DispenserBlock.FACING);
            BlockPos target = blockPointer.getPos().offset(facing);
            BlockState state = world.getBlockState(target);
            Item item = stack.getItem();

            if(state.getBlock() == Blocks.WATER && item == Items.BUCKET){
                world.setBlockState(target, Blocks.AIR.getDefaultState());
                this.setSuccess(true);
                return getBucketableAnimals(world, target);
            }
            this.setSuccess(false);
            return stack;
        }

        @Nullable
        private ItemStack getBucketableAnimals(World world, BlockPos pos) {
            List<Entity> list = world.getEntitiesByClass(Entity.class, new Box(pos.getX(), pos.getY(), pos.getZ(), (pos.getX() + 1), (pos.getY() + 1), (pos.getZ() + 1)), (bucketable) -> bucketable instanceof Bucketable);
            if (list.size() >= 1) {
                list.get(0).remove(Entity.RemovalReason.DISCARDED);
                return ((Bucketable)list.get(0)).getBucketItem();
            } else {
                return new ItemStack(Items.WATER_BUCKET);
            }
        }
    };

    //Additional Movable Blocks
    public static final Tag.Identified<Block> PISTON_OVERRIDE_MOVABLE = BlockTagsAccessor.register("carpet:piston_movable");
    public static final Tag.Identified<Block> PISTON_OVERRIDE_PUSH_ONLY = BlockTagsAccessor.register("carpet:piston_push_only");
    public static final Tag.Identified<Block> PISTON_OVERRIDE_IMMOVABLE = BlockTagsAccessor.register("carpet:piston_immovable");
    public static final Tag.Identified<Block> PISTON_OVERRIDE_DESTROY = BlockTagsAccessor.register("carpet:piston_destroy");
    public static final Tag.Identified<Block> PISTON_OVERRIDE_WEAK_STICKY = BlockTagsAccessor.register("carpet:piston_weak_sticky");

    public static final List<Identifier> CARPET_BLOCK_TAGS = ImmutableList.of(
        SIMPLE_FULL_BLOCK.getId(),
        FULL_CUBE.getId(),
        DISPENSER_BLOCK_BLACKLIST.getId(),
        DISPENSER_BLOCK_WHITELIST.getId(),
        PISTON_OVERRIDE_MOVABLE.getId(),
        PISTON_OVERRIDE_PUSH_ONLY.getId(),
        PISTON_OVERRIDE_IMMOVABLE.getId(),
        PISTON_OVERRIDE_DESTROY.getId(),
        PISTON_OVERRIDE_WEAK_STICKY.getId()
    );

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

    private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(String id, BlockEntityType.BlockEntityFactory<? extends T> supplier, Type<?> type, Block... blocks) {
        return Registry.register(Registry.BLOCK_ENTITY_TYPE, id, new BlockEntityType<>(supplier, ImmutableSet.copyOf(blocks), type));
    }

    public static void init() {
        // initializes statics of CarpetRegistry
    }

    public static boolean isIgnoredForSync(Identifier key) {
        return key.getNamespace().equals("carpet");
    }


    private static MultiDispenserBehavior fireChargeBehavior;
    private static MultiDispenserBehavior shearsBehavior;

    public static DispenserBehavior getDispenserBehavior(Item item, Map<Item, DispenserBehavior> behaviors) {
        if (item == Items.GUNPOWDER) return CarpetRegistry.BREAK_BLOCK_DISPENSER_BEHAVIOR;

        boolean carpet = ItemTags.CARPETS.contains(item);
        boolean powderSnowBucket = item instanceof PowderSnowBucketItem;
        if (Settings.dispensersPlaceBlocks != PlaceBlockDispenserBehavior.Option.FALSE && (!behaviors.containsKey(item) || carpet) && item instanceof BlockItem) {
            if (PlaceBlockDispenserBehavior.canPlace(((BlockItem) item).getBlock())) {
                if (carpet) {
                    return new MultiDispenserBehavior(CarpetRegistry.PLACE_BLOCK_DISPENSER_BEHAVIOR, behaviors.get(item));
                } else if (powderSnowBucket && Settings.dispensersInteractCauldron) {
                    return new MultiDispenserBehavior(CarpetRegistry.DISPENSER_INTERACT_CAULDRON, CarpetRegistry.PLACE_BLOCK_DISPENSER_BEHAVIOR, behaviors.get(item));
                }
                return CarpetRegistry.PLACE_BLOCK_DISPENSER_BEHAVIOR;
            }
        }
        if (Settings.dispensersTillSoil && item instanceof HoeItem) {
            return CarpetRegistry.DISPENSERS_TILL_SOIL_BEHAVIOR;
        }
        if (Settings.dispensersStripLogs && item instanceof AxeItem) {
            return CarpetRegistry.DISPENSERS_STRIP_LOGS_BEHAVIOR;
        }
        if (Settings.renewableNetherrack && item == Items.FIRE_CHARGE) {
            if (fireChargeBehavior == null) {
                fireChargeBehavior = new MultiDispenserBehavior(new FireChargeConvertsToNetherrackBehavior(), behaviors.get(item));
            }
            return fireChargeBehavior;
        }
        if (Settings.dispensersShearVines && item == Items.SHEARS) {
            if (shearsBehavior == null) {
                shearsBehavior = new MultiDispenserBehavior(behaviors.get(item), new ShearVinesBehavior());
            }
            return shearsBehavior;
        }
        if(Settings.smartSaddleDispenser && item == Items.SADDLE) {
            return CarpetRegistry.SMART_SADDLE_DISPENSER_BEHAVIOR;
        }

        if (Settings.dispensersInteractCauldron
                && Settings.dispensersPickupBucketables
                && (item instanceof BucketItem || powderSnowBucket || item == Items.POTION || item == Items.GLASS_BOTTLE)) {
            return new MultiDispenserBehavior(CarpetRegistry.DISPENSER_INTERACT_CAULDRON, CarpetRegistry.DISPENSER_PICKUP_BUCKETABLES, behaviors.get(item));
        }

        if (Settings.dispensersPickupBucketables && item == Items.BUCKET){
            return new MultiDispenserBehavior(CarpetRegistry.DISPENSER_PICKUP_BUCKETABLES, behaviors.get(item));
        }

        if (Settings.dispensersInteractCauldron
                && (item instanceof BucketItem || powderSnowBucket || item == Items.POTION || item == Items.GLASS_BOTTLE)) {
            return new MultiDispenserBehavior(CarpetRegistry.DISPENSER_INTERACT_CAULDRON,  behaviors.get(item));
        }

        return behaviors.get(item);
    }
}
