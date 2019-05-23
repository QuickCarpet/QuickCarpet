package quickcarpet.skyblock;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.village.TradeOffers;
import quickcarpet.utils.Reflection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Trades {
    public static void mergeWanderingTraderOffers(Int2ObjectMap<TradeOffers.Factory[]> custom) {
        List<TradeOffers.Factory> tier1 = new ArrayList<>(Arrays.asList(SkyBlockRegistry.VANILLA_WANDERING_TRADER_OFFERS.get(1)));
        TradeOffers.Factory[] customTier1 = custom.get(1);
        if (customTier1 != null) tier1.addAll(Arrays.asList(customTier1));
        TradeOffers.WANDERING_TRADER_TRADES.put(1, tier1.toArray(new TradeOffers.Factory[0]));
        List<TradeOffers.Factory> tier2 = new ArrayList<>(Arrays.asList(SkyBlockRegistry.VANILLA_WANDERING_TRADER_OFFERS.get(2)));
        TradeOffers.Factory[] customTier2 = custom.get(2);
        if (customTier2 != null) tier2.addAll(Arrays.asList(customTier2));
        TradeOffers.WANDERING_TRADER_TRADES.put(2, tier2.toArray(new TradeOffers.Factory[0]));
    }

    private static TradeOffers.Factory sell(Item item, int price, int maxUses) {
        return Reflection.newSellItemFactory(new ItemStack(item), price, 1, maxUses, 1, 0.05f);
    }

    public static Int2ObjectMap<TradeOffers.Factory[]> getSkyblockWanderingTraderOffers() {
        return new Int2ObjectOpenHashMap<>(ImmutableMap.of(2, new TradeOffers.Factory[] {
            sell(Items.SOUL_SAND, 1, 11),
            sell(Items.LAVA_BUCKET, 16, 9),
            sell(Items.CHORUS_FLOWER, 5, 6),
            sell(Items.JUKEBOX, 64, 6),
            sell(Items.HEART_OF_THE_SEA, 64, 6),
            sell(Items.NETHER_WART, 1, 12),
            sell(Items.COCOA_BEANS, 1, 14),
            sell(Items.SWEET_BERRIES, 1, 16)
        }));
    }
}
