package quickcarpet.mixin.dispenser;

import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.item.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import quickcarpet.annotation.Feature;
import quickcarpet.feature.FireChargeConvertsToNetherrackBehavior;
import quickcarpet.feature.MultiDispenserBehavior;
import quickcarpet.feature.PlaceBlockDispenserBehavior;
import quickcarpet.feature.ShearVinesBehavior;
import quickcarpet.utils.CarpetRegistry;

import java.util.Map;

@Feature("dispensersPlaceBlocks")
@Mixin(DispenserBlock.class)
public abstract class DispenserBlockMixin extends BlockWithEntity {
    @Shadow @Final private static Map<Item, DispenserBehavior> BEHAVIORS;

    protected DispenserBlockMixin(Settings block$Settings_1) {
        super(block$Settings_1);
    }

    private static MultiDispenserBehavior fireChargeBehavior;
    private static MultiDispenserBehavior shearsBehavior;

    /**
     * @author skyrising
     * @reason Only a single expression, equivalent to a redirect on get()
     */
    @Overwrite
    public DispenserBehavior getBehaviorForItem(ItemStack stack) {
        Item item = stack.getItem();
        if (item == Items.GUNPOWDER) return CarpetRegistry.BREAK_BLOCK_DISPENSER_BEHAVIOR;
        if (quickcarpet.settings.Settings.dispensersPlaceBlocks != PlaceBlockDispenserBehavior.Option.FALSE && !BEHAVIORS.containsKey(item) && item instanceof BlockItem) {
            if (PlaceBlockDispenserBehavior.canPlace(((BlockItem) item).getBlock())) return CarpetRegistry.PLACE_BLOCK_DISPENSER_BEHAVIOR;
        }
        if (quickcarpet.settings.Settings.dispensersTillSoil && item instanceof HoeItem) {
            return CarpetRegistry.DISPENSERS_TILL_SOIL_BEHAVIOR;
        }
        if (quickcarpet.settings.Settings.fireChargeConvertsToNetherrack && item == Items.FIRE_CHARGE) {
            if (fireChargeBehavior == null) {
                fireChargeBehavior = new MultiDispenserBehavior(new FireChargeConvertsToNetherrackBehavior(), (ItemDispenserBehavior) BEHAVIORS.get(item));
            }
            return fireChargeBehavior;
        }
        if (quickcarpet.settings.Settings.dispensersShearVines && item == Items.SHEARS) {
            if (shearsBehavior == null) {
                shearsBehavior = new MultiDispenserBehavior((ItemDispenserBehavior) BEHAVIORS.get(item), new ShearVinesBehavior());
            }
            return shearsBehavior;
        }
        return BEHAVIORS.get(item);
    }
}
