package quickcarpet.mixin;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(DispenserBlock.class)
public interface IDispenserBlockMixin
{
    @Accessor("BEHAVIORS")
    static Map<Item, DispenserBehavior> getBehaviour()
    {
        throw new AssertionError();
    }
    
}
