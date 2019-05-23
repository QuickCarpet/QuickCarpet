package quickcarpet.skyblock;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.potion.Potion;
import net.minecraft.village.TradeOffers;

public class SkyBlockRegistry {

    public static final Int2ObjectMap<TradeOffers.Factory[]> VANILLA_WANDERING_TRADER_OFFERS = new Int2ObjectOpenHashMap<>(TradeOffers.WANDERING_TRADER_TRADES);

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
}
