package quickcarpet.skyblock;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import quickcarpet.settings.ChangeListener;
import quickcarpet.settings.ParsedRule;
import quickcarpet.settings.Rule;

import static quickcarpet.settings.RuleCategory.*;

public class SkyBlockSettings {
    @Rule(desc = "Better potions", category = {EXPERIMENTAL, FEATURE}, onChange = BetterPotionListener.class)
    public static boolean betterPotions = false;
    
    enum flowerPotOptions
    { 
        manual,powered,off; 
    } 
    
    @Rule(desc = "Flower pot chunk loading", category = {EXPERIMENTAL, FEATURE}, extra = {"Place a plant in a flowerpot to load the chunk!"})
    public static flowerPotOptions flowerPotChunkLoading = off;

    @Rule(desc = "Add trades to the wandering trader for Skyblock", category = {EXPERIMENTAL, FEATURE}, onChange = WanderingTraderSkyblockTradesChange.class)
    public static boolean wanderingTraderSkyblockTrades = false;

    private static class WanderingTraderSkyblockTradesChange implements ChangeListener<Boolean> {
        @Override
        public void onChange(ParsedRule<Boolean> rule, Boolean previous) {
            if (wanderingTraderSkyblockTrades) {
                Trades.mergeWanderingTraderOffers(Trades.getSkyblockWanderingTraderOffers());
            } else {
                Trades.mergeWanderingTraderOffers(new Int2ObjectOpenHashMap<>());
            }
        }
    }

    @Rule(
        desc = "Block light detector.", extra = {
            "Right click a daylight sensor with a light source to toggle.",
            "No visual indicator besides redstone power is given."
        }, category = {FEATURE, EXPERIMENTAL}
    )
    public static boolean blockLightDetector = false;

    @Rule(desc = "Fixes exit end portal generating too low", extra = "MC-93185", category = FIX)
    public static boolean endPortalFix = true;
}
