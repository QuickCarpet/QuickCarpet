package quickcarpet.mixin.dispenser;

import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import quickcarpet.utils.CarpetRegistry;

import java.util.Map;

@Mixin(DispenserBlock.class)
public abstract class DispenserBlockMixin extends BlockWithEntity {
    @Shadow @Final private static Map<Item, DispenserBehavior> BEHAVIORS;

    protected DispenserBlockMixin(Settings block$Settings_1) {
        super(block$Settings_1);
    }

    /**
     * @author skyrising
     * @reason Only a single expression, equivalent to a redirect on get()
     */
    @Overwrite
    public DispenserBehavior getBehaviorForItem(ItemStack stack) {
        return CarpetRegistry.getDispenserBehavior(stack.getItem(), BEHAVIORS);
    }
}
