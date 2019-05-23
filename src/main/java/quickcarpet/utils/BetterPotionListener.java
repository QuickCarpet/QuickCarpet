package quickcarpet.utils;

import net.minecraft.recipe.BrewingRecipeRegistry;
import quickcarpet.mixin.skyblock.BrewingRecipeRegistryAccessor;
import quickcarpet.settings.ChangeListener;
import quickcarpet.settings.ParsedRule;

public class BetterPotionListener implements ChangeListener
{
    @Override
    public void onChange(ParsedRule rule)
    {
        BrewingRecipeRegistryAccessor.getPotionRecipeList().clear();
        BrewingRecipeRegistry.registerDefaults();
    }
}
