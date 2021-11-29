package quickcarpet.feature.dispenser;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.item.*;
import net.minecraft.tag.ItemTags;
import quickcarpet.settings.Settings;

import java.util.Map;

public final class DispenserBehaviors {
    public static final PlaceBlockBehavior PLACE_BLOCK = new PlaceBlockBehavior();
    public static final BreakBlockBehavior BREAK_BLOCK = new BreakBlockBehavior();
    public static final TillSoilBehavior TILL_SOIL = new TillSoilBehavior();
    public static final StripLogsBehavior STRIP_LOGS = new StripLogsBehavior();
    public static final InteractCauldronBehavior INTERACT_CAULDRON = new InteractCauldronBehavior();
    public static final DispenserBehavior SMART_SADDLE = new SmartSaddleBehavior();
    public static final PickupBucketablesBehavior PICKUP_BUCKETABLES = new PickupBucketablesBehavior();

    private DispenserBehaviors() {}

    public static DispenserBehavior getDispenserBehavior(Item item, Map<Item, DispenserBehavior> behaviors) {
        DispenserBehavior vanilla = behaviors.get(item);
        boolean isDefault = behaviors instanceof Object2ObjectMap<Item, DispenserBehavior> o2oMap && vanilla == o2oMap.defaultReturnValue() && !behaviors.containsKey(item);

        if (item instanceof BlockItem blockItem && PlaceBlockBehavior.canPlace(blockItem.getBlock())) {
            if (ItemTags.CARPETS.contains(item)) return new MultiDispenserBehavior(PLACE_BLOCK, vanilla);
            if (item instanceof PowderSnowBucketItem) {
                if (Settings.dispensersInteractCauldron) return new MultiDispenserBehavior(INTERACT_CAULDRON, vanilla);
                return vanilla;
            }
            if (isDefault) return PLACE_BLOCK;
        }
        if (Settings.dispensersBreakBlocks != BreakBlockBehavior.Option.FALSE && item == Items.GUNPOWDER) return BREAK_BLOCK;
        if (Settings.dispensersTillSoil && item instanceof HoeItem) return TILL_SOIL;
        if (Settings.dispensersStripLogs && item instanceof AxeItem) return STRIP_LOGS;
        if (Settings.smartSaddleDispenser && item == Items.SADDLE) return SMART_SADDLE;
        if (Settings.renewableNetherrack && item == Items.FIRE_CHARGE) {
            return new MultiDispenserBehavior(new FireChargeConvertsToNetherrackBehavior(), behaviors.get(item));
        }
        if (Settings.dispensersShearVines && item == Items.SHEARS) {
            return new MultiDispenserBehavior(behaviors.get(item), new ShearVinesBehavior());
        }
        if (Settings.dispensersInteractCauldron && InteractCauldronBehavior.isCauldronItem(item)) {
            if (Settings.dispensersPickupBucketables) {
                return new MultiDispenserBehavior(INTERACT_CAULDRON, PICKUP_BUCKETABLES, vanilla);
            }
            return new MultiDispenserBehavior(INTERACT_CAULDRON, vanilla);
        }
        if (Settings.dispensersPickupBucketables && item == Items.BUCKET) {
            return new MultiDispenserBehavior(PICKUP_BUCKETABLES, vanilla);
        }
        return vanilla;
    }
}
