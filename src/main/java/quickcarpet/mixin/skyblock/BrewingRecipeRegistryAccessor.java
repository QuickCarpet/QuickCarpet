package quickcarpet.mixin.skyblock;

import net.minecraft.recipe.BrewingRecipeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(BrewingRecipeRegistry.class)
public interface BrewingRecipeRegistryAccessor
{
    @Accessor("POTION_RECIPES")
    static List<?> getPotionRecipeList()
    {
        throw new AssertionError();
    }
}