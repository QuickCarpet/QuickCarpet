package quickcarpet.skyblock;

import net.minecraft.recipe.BrewingRecipeRegistry;
import quickcarpet.settings.ChangeListener;
import quickcarpet.settings.ParsedRule;
import quickcarpet.skyblock.mixin.BrewingRecipeRegistryAccessor;

public class BetterPotionListener implements ChangeListener
{
    @Override
    public void onChange(ParsedRule rule)
    {
        BrewingRecipeRegistryAccessor.getPotionRecipeList().clear();
        BrewingRecipeRegistry.registerDefaults();
    }
}
