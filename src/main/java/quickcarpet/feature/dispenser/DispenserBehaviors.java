package quickcarpet.feature.dispenser;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.item.*;
import net.minecraft.tag.ItemTags;
import quickcarpet.settings.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class DispenserBehaviors {
    public static final PlaceBlockBehavior PLACE_BLOCK = new PlaceBlockBehavior();
    public static final BreakBlockBehavior BREAK_BLOCK = new BreakBlockBehavior();
    public static final TillSoilBehavior TILL_SOIL = new TillSoilBehavior();
    public static final StripLogsBehavior STRIP_LOGS = new StripLogsBehavior();
    public static final InteractCauldronBehavior INTERACT_CAULDRON = new InteractCauldronBehavior();
    public static final DispenserBehavior SMART_SADDLE = new SmartSaddleBehavior();
    public static final PickupBucketablesBehavior PICKUP_BUCKETABLES = new PickupBucketablesBehavior();
    public static final FireChargeConvertsToNetherrackBehavior FIRE_CHARGE = new FireChargeConvertsToNetherrackBehavior();
    public static final ShearVinesBehavior SHEAR_VINES = new ShearVinesBehavior();
    public static final ScrapeCopperBehavior SCRAPE_COPPER = new ScrapeCopperBehavior();
    public static final MilkCowsAndGoatsBehavior MILK_COWS_AND_GOATS = new MilkCowsAndGoatsBehavior();

    private DispenserBehaviors() {}

    public static DispenserBehavior getDispenserBehavior(Item item, Map<Item, DispenserBehavior> behaviors) {
        DispenserBehavior vanilla = behaviors.get(item);
        if (item == Items.GUNPOWDER) return getGunpowderBehavior(vanilla);
        if (item == Items.SADDLE) return getSaddleBehavior(vanilla);
        if (item == Items.FIRE_CHARGE) return getFireChargeBehavior(vanilla);
        if (item == Items.SHEARS) return getShearBehavior(vanilla);
        if (item == Items.BUCKET) return getBucketBehavior(vanilla);
        if (item == Items.POTION || item == Items.GLASS_BOTTLE) return getInteractCauldronBehavior(vanilla);
        if (item instanceof HoeItem) return getHoeBehavior(vanilla);
        if (item instanceof AxeItem) return getAxeBehavior(vanilla);
        if (item instanceof FluidModificationItem) return getInteractCauldronBehavior(vanilla);
        if (item instanceof BlockItem blockItem && PlaceBlockBehavior.canPlace(blockItem.getBlock().getDefaultState())) {
            boolean isDefault = behaviors instanceof Object2ObjectMap<Item, DispenserBehavior> o2oMap && vanilla == o2oMap.defaultReturnValue() && !behaviors.containsKey(item);
            return getBlockItemBehavior(vanilla, item, isDefault);
        }
        return vanilla;
    }

    private static DispenserBehavior getBlockItemBehavior(DispenserBehavior vanilla, Item item, boolean isDefault) {
        if (item.getRegistryEntry().isIn(ItemTags.CARPETS)) return new MultiDispenserBehavior(PLACE_BLOCK, vanilla);
        if (item instanceof PowderSnowBucketItem) {
            if (Settings.dispensersInteractCauldron) return new MultiDispenserBehavior(INTERACT_CAULDRON, vanilla);
            return vanilla;
        }
        if (isDefault) return PLACE_BLOCK;
        return vanilla;
    }

    private static DispenserBehavior getGunpowderBehavior(DispenserBehavior vanilla) {
        return Settings.dispensersBreakBlocks != BreakBlockBehavior.Option.FALSE ? BREAK_BLOCK : vanilla;
    }

    private static DispenserBehavior getSaddleBehavior(DispenserBehavior vanilla) {
        return Settings.smartSaddleDispenser ? SMART_SADDLE : vanilla;
    }

    private static DispenserBehavior getFireChargeBehavior(DispenserBehavior vanilla) {
        return Settings.renewableNetherrack ? new MultiDispenserBehavior(FIRE_CHARGE, vanilla) : vanilla;
    }

    private static DispenserBehavior getShearBehavior(DispenserBehavior vanilla) {
        return Settings.dispensersShearVines ? new MultiDispenserBehavior(vanilla, SHEAR_VINES) : vanilla;
    }

    private static DispenserBehavior getBucketBehavior(DispenserBehavior vanilla) {
        List<DispenserBehavior> matching = new ArrayList<>(3);
        if (Settings.dispensersInteractCauldron) matching.add(INTERACT_CAULDRON);
        if (Settings.dispensersPickupBucketables) matching.add(PICKUP_BUCKETABLES);
        if (Settings.dispensersMilkCowsAndGoats) matching.add(MILK_COWS_AND_GOATS);
        matching.add(vanilla);
        return MultiDispenserBehavior.of(matching);
    }

    private static DispenserBehavior getInteractCauldronBehavior(DispenserBehavior vanilla) {
        return Settings.dispensersInteractCauldron ? new MultiDispenserBehavior(INTERACT_CAULDRON, vanilla) : vanilla;
    }

    private static DispenserBehavior getHoeBehavior(DispenserBehavior vanilla) {
        return Settings.dispensersTillSoil ? TILL_SOIL : vanilla;
    }

    private static DispenserBehavior getAxeBehavior(DispenserBehavior vanilla) {
        List<DispenserBehavior> matching = new ArrayList<>(2);
        if (Settings.dispensersStripLogs) matching.add(STRIP_LOGS);
        if (Settings.dispensersScrapeCopper) matching.add(SCRAPE_COPPER);
        if (matching.isEmpty()) return vanilla;
        return MultiDispenserBehavior.of(matching);
    }
}
