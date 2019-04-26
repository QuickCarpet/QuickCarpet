package quickcarpet.mixin.skyblock;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.Items;
import net.minecraft.village.TradeOffers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import quickcarpet.utils.SellItemFactory;

@Mixin(TradeOffers.class)
public abstract class MixinTradeOffers
{
    @Mutable
    @Shadow
    @Final
    public static Int2ObjectMap<TradeOffers.Factory[]> WANDERING_TRADER_TRADES;
    
    static
    {
        WANDERING_TRADER_TRADES = copyToFastUtilMap(ImmutableMap.of(1, new TradeOffers.Factory[]{new SellItemFactory(Items.SEA_PICKLE, 2, 1, 5, 1), new SellItemFactory(Items.SLIME_BALL, 4, 1, 5, 1), new SellItemFactory(Items.GLOWSTONE, 2, 1, 5, 1), new SellItemFactory(Items.NAUTILUS_SHELL, 5, 1, 5, 1), new SellItemFactory(Items.FERN, 1, 1, 12, 1), new SellItemFactory(Items.SUGAR_CANE, 1, 1, 8, 1), new SellItemFactory(Items.PUMPKIN, 1, 1, 4, 1), new SellItemFactory(Items.KELP, 3, 1, 12, 1), new SellItemFactory(Items.CACTUS, 3, 1, 8, 1), new SellItemFactory(Items.DANDELION, 1, 1, 12, 1), new SellItemFactory(Items.POPPY, 1, 1, 12, 1), new SellItemFactory(Items.BLUE_ORCHID, 1, 1, 8, 1), new SellItemFactory(Items.ALLIUM, 1, 1, 12, 1), new SellItemFactory(Items.AZURE_BLUET, 1, 1, 12, 1), new SellItemFactory(Items.RED_TULIP, 1, 1, 12, 1), new SellItemFactory(Items.ORANGE_TULIP, 1, 1, 12, 1), new SellItemFactory(Items.WHITE_TULIP, 1, 1, 12, 1), new SellItemFactory(Items.PINK_TULIP, 1, 1, 12, 1), new SellItemFactory(Items.OXEYE_DAISY, 1, 1, 12, 1), new SellItemFactory(Items.CORNFLOWER, 1, 1, 12, 1), new SellItemFactory(Items.LILY_OF_THE_VALLEY, 1, 1, 7, 1), new SellItemFactory(Items.WHEAT_SEEDS, 1, 1, 12, 1), new SellItemFactory(Items.BEETROOT_SEEDS, 1, 1, 12, 1), new SellItemFactory(Items.PUMPKIN_SEEDS, 1, 1, 12, 1), new SellItemFactory(Items.MELON_SEEDS, 1, 1, 12, 1), new SellItemFactory(Items.ACACIA_SAPLING, 5, 1, 8, 1), new SellItemFactory(Items.BIRCH_SAPLING, 5, 1, 8, 1), new SellItemFactory(Items.DARK_OAK_SAPLING, 5, 1, 8, 1), new SellItemFactory(Items.JUNGLE_SAPLING, 5, 1, 8, 1), new SellItemFactory(Items.OAK_SAPLING, 5, 1, 8, 1), new SellItemFactory(Items.SPRUCE_SAPLING, 5, 1, 8, 1), new SellItemFactory(Items.RED_DYE, 1, 3, 12, 1), new SellItemFactory(Items.WHITE_DYE, 1, 3, 12, 1), new SellItemFactory(Items.BLUE_DYE, 1, 3, 12, 1), new SellItemFactory(Items.PINK_DYE, 1, 3, 12, 1), new SellItemFactory(Items.BLACK_DYE, 1, 3, 12, 1), new SellItemFactory(Items.GREEN_DYE, 1, 3, 12, 1), new SellItemFactory(Items.LIGHT_GRAY_DYE, 1, 3, 12, 1), new SellItemFactory(Items.MAGENTA_DYE, 1, 3, 12, 1), new SellItemFactory(Items.YELLOW_DYE, 1, 3, 12, 1), new SellItemFactory(Items.GRAY_DYE, 1, 3, 12, 1), new SellItemFactory(Items.PURPLE_DYE, 1, 3, 12, 1), new SellItemFactory(Items.LIGHT_BLUE_DYE, 1, 3, 12, 1), new SellItemFactory(Items.LIME_DYE, 1, 3, 12, 1), new SellItemFactory(Items.ORANGE_DYE, 1, 3, 12, 1), new SellItemFactory(Items.BROWN_DYE, 1, 3, 12, 1), new SellItemFactory(Items.CYAN_DYE, 1, 3, 12, 1), new SellItemFactory(Items.BRAIN_CORAL_BLOCK, 3, 1, 8, 1), new SellItemFactory(Items.BUBBLE_CORAL_BLOCK, 3, 1, 8, 1), new SellItemFactory(Items.FIRE_CORAL_BLOCK, 3, 1, 8, 1), new SellItemFactory(Items.HORN_CORAL_BLOCK, 3, 1, 8, 1), new SellItemFactory(Items.TUBE_CORAL_BLOCK, 3, 1, 8, 1), new SellItemFactory(Items.VINE, 1, 1, 12, 1), new SellItemFactory(Items.BROWN_MUSHROOM, 1, 1, 12, 1), new SellItemFactory(Items.RED_MUSHROOM, 1, 1, 12, 1), new SellItemFactory(Items.LILY_PAD, 1, 2, 5, 1), new SellItemFactory(Items.SAND, 1, 8, 8, 1), new SellItemFactory(Items.RED_SAND, 1, 4, 6, 1)}, 2, new TradeOffers.Factory[]{new SellItemFactory(Items.TROPICAL_FISH_BUCKET, 5, 1, 4, 1), new SellItemFactory(Items.PUFFERFISH_BUCKET, 5, 1, 4, 1), new SellItemFactory(Items.PACKED_ICE, 3, 1, 6, 1), new SellItemFactory(Items.BLUE_ICE, 6, 1, 6, 1), new SellItemFactory(Items.GUNPOWDER, 1, 1, 8, 1), new SellItemFactory(Items.PODZOL, 3, 3, 6, 1), new SellItemFactory(Items.SOUL_SAND, 1, 1, 11, 1), new SellItemFactory(Items.LAVA_BUCKET, 1, 1, 9, 1), new SellItemFactory(Items.CHORUS_FLOWER, 5, 1, 6, 1), new SellItemFactory(Items.JUKEBOX, 64, 1, 6, 1), new SellItemFactory(Items.HEART_OF_THE_SEA, 64, 1, 6, 1)}));
    }
    
    @Shadow
    protected static Int2ObjectMap<TradeOffers.Factory[]> copyToFastUtilMap(ImmutableMap<Integer, TradeOffers.Factory[]> immutableMap_1)
    {
        return null;
    }
    
    
}
