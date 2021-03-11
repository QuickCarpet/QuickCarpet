package quickcarpet.utils;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.*;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.util.math.Direction;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import quickcarpet.QuickCarpet;
import quickcarpet.utils.Messenger.Formatter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static net.minecraft.entity.data.TrackedDataHandlerRegistry.*;

public final class DataTrackerUtils {
    private static final Logger LOGGER = LogManager.getLogger();

    private DataTrackerUtils() {}

    @SuppressWarnings("unchecked")
    public enum KnownType {
        UNKNOWN(null, Formatter.OBJECT),
        BYTE(TrackedDataHandlerRegistry.BYTE, Formatter.NUMBER),
        INTEGER(TrackedDataHandlerRegistry.INTEGER, Formatter.NUMBER),
        FLOAT(TrackedDataHandlerRegistry.FLOAT, Formatter.FLOAT),
        STRING(TrackedDataHandlerRegistry.STRING, Formatter.STRING),
        TEXT_COMPONENT(TrackedDataHandlerRegistry.TEXT_COMPONENT, Formatter.TEXT),
        OPTIONAL_TEXT_COMPONENT(TrackedDataHandlerRegistry.OPTIONAL_TEXT_COMPONENT, Formatter.optional(Formatter.TEXT)),
        ITEM_STACK(TrackedDataHandlerRegistry.ITEM_STACK, Formatter.ITEM_STACK),
        BOOLEAN(TrackedDataHandlerRegistry.BOOLEAN, Formatter.BOOLEAN),
        ROTATION(TrackedDataHandlerRegistry.ROTATION, Formatter.ROTATION),
        BLOCK_POS(TrackedDataHandlerRegistry.BLOCK_POS, Formatter.BLOCK_POS),
        OPTIONAL_BLOCK_POS(TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS, Formatter.optional(Formatter.BLOCK_POS)),
        FACING(TrackedDataHandlerRegistry.FACING, (Formatter<Direction>) Formatter.ENUM),
        OPTIONAL_UUID(TrackedDataHandlerRegistry.OPTIONAL_UUID, Formatter.optional((Formatter<UUID>) Formatter.OBJECT)),
        OPTIONAL_BLOCK_STATE(TrackedDataHandlerRegistry.OPTIONAL_BLOCK_STATE, Formatter.optional(Formatter.BLOCK_STATE)),
        COMPOUND_TAG(TrackedDataHandlerRegistry.TAG_COMPOUND, Formatter.COMPOUND_TAG),
        PARTICLE(TrackedDataHandlerRegistry.PARTICLE, Formatter.PARTICLE),
        VILLAGER_DATA(TrackedDataHandlerRegistry.VILLAGER_DATA, Formatter.VILLAGER_DATA),
        ENTITY_ID(TrackedDataHandlerRegistry.FIREWORK_DATA, Formatter.OPTIONAL_INT),
        ENTITY_POSE(TrackedDataHandlerRegistry.ENTITY_POSE, (Formatter<EntityPose>) Formatter.ENUM),
        ;

        private static final Map<TrackedDataHandler<?>, KnownType> TYPE_MAP = new IdentityHashMap<>();
        public final TrackedDataHandler<?> handler;
        public final Formatter<?> formatter;

        <T> KnownType(TrackedDataHandler<T> handler, Formatter<? super T> formatter) {
            this.handler = handler;
            this.formatter = formatter;
        }

        static {
            for (KnownType t : values()) if (t.handler != null) TYPE_MAP.put(t.handler, t);
        }

        public static KnownType get(TrackedDataHandler<?> handler) {
            return TYPE_MAP.getOrDefault(handler, UNKNOWN);
        }
    }

    static final Map<Class<? extends Entity>, Int2ObjectMap<Pair<String, KnownType>>> KNOWN_PROPERTIES = new HashMap<>();
    private static final Set<Class<? extends Entity>> LOCKED = new HashSet<>();

    private static void register(Class<? extends Entity> entityClass, TrackedDataHandler<?> type, String name) {
        if (QuickCarpet.isDevelopment() && LOCKED.contains(entityClass)) {
            throw new IllegalStateException("Property '" + name + "' for " + entityClass.getSimpleName() + " registered after subclass");
        }
        Int2ObjectMap<Pair<String, KnownType>> entityProperties = KNOWN_PROPERTIES.computeIfAbsent(entityClass, k -> new Int2ObjectLinkedOpenHashMap<>());
        int id = 0;
        for (Class<? extends Entity> cls : Reflection.iterateSuperClasses(entityClass, Entity.class)) {
            // Make sure the classes are initialized so the our and vanilla's properties match up
            try {
                Class.forName(cls.getName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            if (cls != entityClass) LOCKED.add(cls);
            if (KNOWN_PROPERTIES.containsKey(cls)) {
                id += KNOWN_PROPERTIES.get(cls).size();
            }
        }
        entityProperties.put(id, Pair.of(name, KnownType.get(type)));
    }

    public static Int2ObjectMap<Pair<String, KnownType>> collectKnownProperties(Class<? extends Entity> entityClass) {
        Int2ObjectMap<Pair<String, KnownType>> map = new Int2ObjectOpenHashMap<>();
        for (Class<? extends Entity> cls : Reflection.iterateSuperClasses(entityClass, Entity.class)) {
            if (KNOWN_PROPERTIES.containsKey(cls)) {
                map.putAll(KNOWN_PROPERTIES.get(cls));
            }
        }
        return map;
    }

    public static void check() {
        Map<Class<? extends Entity>, Integer> vanillaCounts = getVanillaPropertyCounts();
        if (vanillaCounts == null) {
            LOGGER.error("Could not get vanilla data tracker property counts");
            return;
        }
        Set<Class<? extends Entity>> vanillaTypes = vanillaCounts.keySet();
        Set<Class<? extends Entity>> knownTypes = KNOWN_PROPERTIES.keySet();
        Set<Class<? extends Entity>> allTypes = new LinkedHashSet<>();
        allTypes.addAll(knownTypes);
        allTypes.addAll(vanillaTypes);
        if (vanillaTypes.size() < allTypes.size()) {
            for (Class<? extends Entity> cls : allTypes) {
                if (!vanillaTypes.contains(cls)) LOGGER.error("Extra entity " + cls.getSimpleName());
            }
        }
        if (knownTypes.size() < allTypes.size()) {
            for (Class<? extends Entity> cls : allTypes) {
                if (!knownTypes.contains(cls)) LOGGER.error("Missing entity " + cls.getSimpleName());
            }
        }
        for (Class<? extends Entity> cls : allTypes) {
            int vanillaCount = vanillaCounts.getOrDefault(cls, -1) + 1;
            int knownCount = collectKnownProperties(cls).size();
            if (vanillaCount != knownCount) {
                LOGGER.error("Mismatching property count for " + cls.getSimpleName() + ": expected " + vanillaCount + ", got " + knownCount);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<Class<? extends Entity>, Integer> getVanillaPropertyCounts() {
        for (Field f : DataTracker.class.getDeclaredFields()) {
            if ((f.getModifiers() & Modifier.STATIC) == 0) continue;
            if (f.getType() != Map.class) continue;
            f.setAccessible(true);
            try {
                return (Map<Class<? extends Entity>, Integer>) f.get(null);
            } catch (IllegalAccessException e) {
                return null;
            }
        }
        return null;
    }

    static {
        register(Entity.class, BYTE, "flags");
        register(Entity.class, INTEGER, "air");
        register(Entity.class, OPTIONAL_TEXT_COMPONENT, "custom_name");
        register(Entity.class, BOOLEAN, "custom_name_visible");
        register(Entity.class, BOOLEAN, "silent");
        register(Entity.class, BOOLEAN, "no_gravity");
        register(Entity.class, ENTITY_POSE, "pose");
        register(AbstractFireballEntity.class, ITEM_STACK, "item");
        register(LivingEntity.class, BYTE, "living_flags");
        register(LivingEntity.class, FLOAT, "health");
        register(LivingEntity.class, INTEGER, "potion_color");
        register(LivingEntity.class, BOOLEAN, "potion_ambient");
        register(LivingEntity.class, INTEGER, "arrow_count");
        register(LivingEntity.class, INTEGER, "stinger_count");
        register(LivingEntity.class, OPTIONAL_BLOCK_POS, "sleeping_position");
        register(MobEntity.class, BYTE, "mob_flags");
        register(PassiveEntity.class, BOOLEAN, "child");
        register(HorseBaseEntity.class, BYTE, "horse_flags");
        register(HorseBaseEntity.class, OPTIONAL_UUID, "owner_uuid");
        register(AbstractDonkeyEntity.class, BOOLEAN, "chest");
        register(AbstractMinecartEntity.class, INTEGER, "hit_time");
        register(AbstractMinecartEntity.class, INTEGER, "hit_direction");
        register(AbstractMinecartEntity.class, FLOAT, "damage");
        register(AbstractMinecartEntity.class, INTEGER, "block_id");
        register(AbstractMinecartEntity.class, INTEGER, "block_offset");
        register(AbstractMinecartEntity.class, BOOLEAN, "block_present");
        register(AbstractPiglinEntity.class, BOOLEAN, "immune");
        register(AreaEffectCloudEntity.class, FLOAT, "radius");
        register(AreaEffectCloudEntity.class, INTEGER, "color");
        register(AreaEffectCloudEntity.class, BOOLEAN, "waiting");
        register(AreaEffectCloudEntity.class, PARTICLE, "particle");
        register(ArmorStandEntity.class, BYTE, "armor_stand_flags");
        register(ArmorStandEntity.class, ROTATION, "head_rotation");
        register(ArmorStandEntity.class, ROTATION, "body_rotation");
        register(ArmorStandEntity.class, ROTATION, "left_arm_rotation");
        register(ArmorStandEntity.class, ROTATION, "right_arm_rotation");
        register(ArmorStandEntity.class, ROTATION, "left_leg_rotation");
        register(ArmorStandEntity.class, ROTATION, "right_leg_rotation");
        register(PersistentProjectileEntity.class, BYTE, "projectile_flags");
        register(PersistentProjectileEntity.class, BYTE, "piercing_level");
        register(ArrowEntity.class, INTEGER, "color");
        register(BatEntity.class, BYTE, "bat_flags");
        register(BeeEntity.class, BYTE, "bee_flags");
        register(BeeEntity.class, INTEGER, "anger");
        register(BlazeEntity.class, BYTE, "blaze_flags");
        register(BoatEntity.class, INTEGER, "hit_time");
        register(BoatEntity.class, INTEGER, "hit_direction");
        register(BoatEntity.class, FLOAT, "damage");
        register(BoatEntity.class, INTEGER, "type");
        register(BoatEntity.class, BOOLEAN, "paddle_left");
        register(BoatEntity.class, BOOLEAN, "paddle_right");
        register(BoatEntity.class, INTEGER, "bubble_time");
        register(TameableEntity.class, BYTE, "tameable_flags");
        register(TameableEntity.class, OPTIONAL_UUID, "owner");
        register(CatEntity.class, INTEGER, "type");
        register(CatEntity.class, BOOLEAN, "sleeping_with_owner");
        register(CatEntity.class, BOOLEAN, "head_down");
        register(CatEntity.class, INTEGER, "collar_color");
        register(CommandBlockMinecartEntity.class, STRING, "command");
        register(CommandBlockMinecartEntity.class, TEXT_COMPONENT, "last_output");
        register(CreeperEntity.class, INTEGER, "fuse_speed");
        register(CreeperEntity.class, BOOLEAN, "charged");
        register(CreeperEntity.class, BOOLEAN, "ignited");
        register(DolphinEntity.class, BLOCK_POS, "treasure_pos");
        register(DolphinEntity.class, BOOLEAN, "has_fish");
        register(DolphinEntity.class, INTEGER, "moistness");
        register(EndCrystalEntity.class, OPTIONAL_BLOCK_POS, "beam_target");
        register(EndCrystalEntity.class, BOOLEAN, "show_bottom");
        register(EnderDragonEntity.class, INTEGER, "phase_type");
        register(EndermanEntity.class, OPTIONAL_BLOCK_STATE, "carried_block");
        register(EndermanEntity.class, BOOLEAN, "angry");
        register(EndermanEntity.class, BOOLEAN, "provoked");
        register(EyeOfEnderEntity.class, ITEM_STACK, "item");
        register(FallingBlockEntity.class, BLOCK_POS, "start_pos");
        register(FireworkRocketEntity.class, ITEM_STACK, "item");
        register(FireworkRocketEntity.class, FIREWORK_DATA, "shooter");
        register(FireworkRocketEntity.class, BOOLEAN, "shot_at_angle");
        register(FishEntity.class, BOOLEAN, "from_bucket");
        register(FishingBobberEntity.class, INTEGER, "hooked_entity");
        register(FishingBobberEntity.class, BOOLEAN, "caught_fish");
        register(FoxEntity.class, INTEGER, "type");
        register(FoxEntity.class, BYTE, "fox_flags");
        register(FoxEntity.class, OPTIONAL_UUID, "owner");
        register(FoxEntity.class, OPTIONAL_UUID, "other_trusted");
        register(FurnaceMinecartEntity.class, BOOLEAN, "lit");
        register(GhastEntity.class, BOOLEAN, "shooting");
        register(GuardianEntity.class, BOOLEAN, "spikes_retracted");
        register(GuardianEntity.class, INTEGER, "beam_target_id");
        register(HoglinEntity.class, BOOLEAN, "baby");
        register(HorseEntity.class, INTEGER, "variant");
        register(IronGolemEntity.class, BYTE, "iron_golem_flags");
        register(ItemEntity.class, ITEM_STACK, "stack");
        register(ItemFrameEntity.class, ITEM_STACK, "item");
        register(ItemFrameEntity.class, INTEGER, "rotation");
        register(LlamaEntity.class, INTEGER, "strength");
        register(LlamaEntity.class, INTEGER, "carpet_color");
        register(LlamaEntity.class, INTEGER, "variant");
        register(MerchantEntity.class, INTEGER, "head_shake_timer");
        register(ThrownItemEntity.class, ITEM_STACK, "item");
        register(MooshroomEntity.class, STRING, "type");
        register(OcelotEntity.class, BOOLEAN, "trusting");
        register(PandaEntity.class, INTEGER, "ask_for_bamboo_ticks");
        register(PandaEntity.class, INTEGER, "sneeze_progress");
        register(PandaEntity.class, INTEGER, "eating_ticks");
        register(PandaEntity.class, BYTE, "main_gene");
        register(PandaEntity.class, BYTE, "hidden_gene");
        register(PandaEntity.class, BYTE, "panda_flags");
        register(ParrotEntity.class, INTEGER, "variant");
        register(PhantomEntity.class, INTEGER, "size");
        register(PigEntity.class, BOOLEAN, "saddled");
        register(PigEntity.class, INTEGER, "boost_time");
        register(PiglinEntity.class, BOOLEAN, "baby");
        register(PiglinEntity.class, BOOLEAN, "charging");
        register(PiglinEntity.class, BOOLEAN, "dancing");
        register(RaiderEntity.class, BOOLEAN, "celebrating");
        register(PillagerEntity.class, BOOLEAN, "charging");
        register(PlayerEntity.class, FLOAT, "absorption");
        register(PlayerEntity.class, INTEGER, "score");
        register(PlayerEntity.class, BYTE, "model_parts");
        register(PlayerEntity.class, BYTE, "main_hand");
        register(PlayerEntity.class, TAG_COMPOUND, "left_shoulder_entity");
        register(PlayerEntity.class, TAG_COMPOUND, "right_shoulder_entity");
        register(PolarBearEntity.class, BOOLEAN, "warning");
        register(PufferfishEntity.class, INTEGER, "puff_state");
        register(RabbitEntity.class, INTEGER, "type");
        register(SheepEntity.class, BYTE, "color");
        register(ShulkerEntity.class, FACING, "attached_face");
        register(ShulkerEntity.class, OPTIONAL_BLOCK_POS, "attached_block");
        register(ShulkerEntity.class, BYTE, "peek_amount");
        register(ShulkerEntity.class, BYTE, "color");
        register(SlimeEntity.class, INTEGER, "size");
        register(SnowGolemEntity.class, BYTE, "snow_golem_flags");
        register(SpellcastingIllagerEntity.class, BYTE, "spell");
        register(SpiderEntity.class, BYTE, "spider_flags");
        register(StriderEntity.class, INTEGER, "boost_time");
        register(StriderEntity.class, BOOLEAN, "cold");
        register(StriderEntity.class, BOOLEAN, "saddled");
        register(TntEntity.class, INTEGER, "fuse");
        register(TridentEntity.class, INTEGER, "loyalty_level");
        register(TridentEntity.class, BOOLEAN, "enchanted");
        register(TropicalFishEntity.class, INTEGER, "variant");
        register(TurtleEntity.class, BLOCK_POS, "home_pos");
        register(TurtleEntity.class, BOOLEAN, "has_egg");
        register(TurtleEntity.class, BOOLEAN, "digging_sand");
        register(TurtleEntity.class, BLOCK_POS, "travel_pos");
        register(TurtleEntity.class, BOOLEAN, "land_bound");
        register(TurtleEntity.class, BOOLEAN, "travelling");
        register(VexEntity.class, BYTE, "vex_flags");
        register(VillagerEntity.class, VILLAGER_DATA, "villager_data");
        register(WitchEntity.class, BOOLEAN, "drinking");
        register(WitherEntity.class, INTEGER, "center_target");
        register(WitherEntity.class, INTEGER, "left_target");
        register(WitherEntity.class, INTEGER, "right_target");
        register(WitherEntity.class, INTEGER, "invulnerable");
        register(WitherSkullEntity.class, BOOLEAN, "charged");
        register(WolfEntity.class, BOOLEAN, "begging");
        register(WolfEntity.class, INTEGER, "collar_color");
        register(WolfEntity.class, INTEGER, "anger_time");
        register(ZoglinEntity.class, BOOLEAN, "baby");
        register(ZombieEntity.class, BOOLEAN, "baby");
        register(ZombieEntity.class, INTEGER, "type");
        register(ZombieEntity.class, BOOLEAN, "converting");
        register(ZombieVillagerEntity.class, BOOLEAN, "converting");
        register(ZombieVillagerEntity.class, VILLAGER_DATA, "villager_data");
    }
}
