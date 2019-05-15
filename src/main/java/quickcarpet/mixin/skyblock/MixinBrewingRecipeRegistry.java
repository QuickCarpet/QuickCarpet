package quickcarpet.mixin.skyblock;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.settings.Settings;

import static quickcarpet.utils.CarpetRegistry.*;

@Mixin(BrewingRecipeRegistry.class)
public abstract class MixinBrewingRecipeRegistry
{
    @Shadow
    protected static void registerPotionRecipe(Potion potion_1, Item item_1, Potion potion_2)
    {
    }
    
    @Inject(method = "registerDefaults", at = @At(value = "TAIL"))
    private static void onRegisterDefaults(CallbackInfo ci)
    {
        if (Settings.betterPotions)
        {
            registerPotionRecipe(Potions.LONG_NIGHT_VISION, Items.REDSTONE_BLOCK, SUPER_LONG_NIGHT_VISION);
            registerPotionRecipe(Potions.LONG_INVISIBILITY, Items.REDSTONE_BLOCK, SUPER_LONG_INVISIBILITY);
            registerPotionRecipe(Potions.LONG_LEAPING, Items.REDSTONE_BLOCK, SUPER_LONG_LEAPING);
            registerPotionRecipe(Potions.STRONG_LEAPING, Items.REDSTONE_BLOCK, SUPER_STRONG_LEAPING);
            registerPotionRecipe(Potions.LONG_FIRE_RESISTANCE, Items.REDSTONE_BLOCK, SUPER_LONG_FIRE_RESISTANCE);
            registerPotionRecipe(Potions.LONG_SWIFTNESS, Items.REDSTONE_BLOCK, SUPER_LONG_SWIFTNESS);
            registerPotionRecipe(Potions.STRONG_SWIFTNESS, Items.REDSTONE_BLOCK, SUPER_STRONG_SWIFTNESS);
            registerPotionRecipe(Potions.LONG_SLOWNESS, Items.REDSTONE_BLOCK, SUPER_LONG_SLOWNESS);
            registerPotionRecipe(Potions.STRONG_SLOWNESS, Items.REDSTONE_BLOCK, SUPER_STRONG_SLOWNESS);
            registerPotionRecipe(Potions.LONG_TURTLE_MASTER, Items.REDSTONE_BLOCK, SUPER_LONG_TURTLE_MASTER);
            registerPotionRecipe(Potions.STRONG_TURTLE_MASTER, Items.REDSTONE_BLOCK, SUPER_STRONG_TURTLE_MASTER);
            registerPotionRecipe(Potions.LONG_WATER_BREATHING, Items.REDSTONE_BLOCK, SUPER_LONG_WATER_BREATHING);
            registerPotionRecipe(Potions.LONG_POISON, Items.REDSTONE_BLOCK, SUPER_LONG_POISON);
            registerPotionRecipe(Potions.STRONG_POISON, Items.REDSTONE_BLOCK, SUPER_STRONG_POISON);
            registerPotionRecipe(Potions.LONG_REGENERATION, Items.REDSTONE_BLOCK, SUPER_LONG_REGENERATION);
            registerPotionRecipe(Potions.STRONG_REGENERATION, Items.REDSTONE_BLOCK, SUPER_STRONG_REGENERATION);
            registerPotionRecipe(Potions.LONG_STRENGTH, Items.REDSTONE_BLOCK, SUPER_LONG_STRENGTH);
            registerPotionRecipe(Potions.STRONG_STRENGTH, Items.REDSTONE_BLOCK, SUPER_STRONG_STRENGTH);
            registerPotionRecipe(Potions.LONG_WEAKNESS, Items.REDSTONE_BLOCK, SUPER_LONG_WEAKNESS);
            registerPotionRecipe(Potions.LONG_SLOW_FALLING, Items.REDSTONE_BLOCK, SUPER_LONG_SLOW_FALLING);
        }
    }
    
    @Redirect(method = "craft", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/potion/PotionUtil;setPotion(Lnet/minecraft/item/ItemStack;" +
                             "Lnet/minecraft/potion/Potion;)Lnet/minecraft/item/ItemStack;"))
    private static ItemStack onCraft(ItemStack itemStack_1, Potion potion)
    {
        if (
            potion == SUPER_LONG_NIGHT_VISION  || potion == SUPER_LONG_INVISIBILITY    || potion == SUPER_LONG_LEAPING    ||
            potion == SUPER_STRONG_LEAPING     || potion == SUPER_LONG_FIRE_RESISTANCE || potion == SUPER_LONG_SWIFTNESS  ||
            potion == SUPER_STRONG_SWIFTNESS   || potion == SUPER_LONG_WATER_BREATHING || potion == SUPER_STRONG_SLOWNESS ||
            potion == SUPER_LONG_TURTLE_MASTER || potion == SUPER_STRONG_TURTLE_MASTER || potion == SUPER_STRONG_STRENGTH ||
            potion == SUPER_LONG_SLOWNESS      || potion == SUPER_LONG_POISON          || potion == SUPER_STRONG_POISON   ||
            potion == SUPER_LONG_REGENERATION  || potion == SUPER_STRONG_REGENERATION  || potion == SUPER_LONG_STRENGTH   ||
            potion == SUPER_LONG_WEAKNESS      || potion == SUPER_LONG_SLOW_FALLING
            )
        {
            PotionUtil.setCustomPotionEffects(itemStack_1, potion.getEffects());
            itemStack_1.setDisplayName(new TranslatableComponent(potion.getName(itemStack_1.getItem().getTranslationKey() + ".effect.")));
            itemStack_1.getTag().putInt("CustomPotionColor", PotionUtil.getColor(potion));
            return itemStack_1;
        }
        else
        {
            return PotionUtil.setPotion(itemStack_1, potion);
        }
    }
}
