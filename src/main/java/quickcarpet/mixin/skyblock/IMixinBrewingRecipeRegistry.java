package quickcarpet.mixin.skyblock;

import net.minecraft.potion.Potion;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(BrewingRecipeRegistry.class)
public interface IMixinBrewingRecipeRegistry
{
    @Accessor("POTION_RECIPES")
    static List<Recipe<Potion>> getPotionRecipeList()
    {
        throw new AssertionError();
    }
}

class Recipe<T> {
    private final T input;
    private final Ingredient ingredient;
    private final T output;
    
    public Recipe(T object_1, Ingredient ingredient_1, T object_2) {
        this.input = object_1;
        this.ingredient = ingredient_1;
        this.output = object_2;
    }
}