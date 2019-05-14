package quickcarpet.utils;

import net.minecraft.recipe.BrewingRecipeRegistry;
import quickcarpet.mixin.skyblock.IMixinBrewingRecipeRegistry;
import quickcarpet.settings.ChangeListener;
import quickcarpet.settings.ParsedRule;

public class BetterPotionListener implements ChangeListener
{
    @Override
    public void onChange(ParsedRule rule)
    {
        IMixinBrewingRecipeRegistry.getPotionRecipeList().clear();
        BrewingRecipeRegistry.registerDefaults();
    }
}
