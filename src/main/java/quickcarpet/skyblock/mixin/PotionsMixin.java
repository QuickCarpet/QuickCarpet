package quickcarpet.skyblock.mixin;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static quickcarpet.skyblock.SkyBlockRegistry.*;

@Mixin(Potions.class)
public abstract class PotionsMixin
{
    static
    {
        SUPER_LONG_NIGHT_VISION = register("super_long_night_vision", new Potion("night_vision", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.NIGHT_VISION, 36000)}));
        SUPER_LONG_INVISIBILITY = register("super_long_invisibility", new Potion("invisibility", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.INVISIBILITY, 36000)}));
        SUPER_LONG_LEAPING = register("super_long_leaping", new Potion("leaping", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.JUMP_BOOST, 36000)}));
        SUPER_STRONG_LEAPING = register("super_strong_leaping", new Potion("leaping", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.JUMP_BOOST, 18000, 1)}));
        SUPER_LONG_FIRE_RESISTANCE = register("super_long_fire_resistance", new Potion("fire_resistance", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 36000)}));
        SUPER_LONG_SWIFTNESS = register("super_long_swiftness", new Potion("swiftness", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.SPEED, 36000)}));
        SUPER_STRONG_SWIFTNESS = register("super_strong_swiftness", new Potion("swiftness", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.SPEED, 18000, 1)}));
        SUPER_LONG_SLOWNESS = register("super_long_slowness", new Potion("slowness", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.SLOWNESS, 18000)}));
        SUPER_STRONG_SLOWNESS = register("super_strong_slowness", new Potion("slowness", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.SLOWNESS, 4000, 3)}));
        SUPER_LONG_TURTLE_MASTER = register("super_long_turtle_master", new Potion("turtle_master", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.SLOWNESS, 3000, 3), new StatusEffectInstance(StatusEffects.RESISTANCE, 3000, 2)}));
        SUPER_STRONG_TURTLE_MASTER = register("super_strong_turtle_master", new Potion("turtle_master", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.SLOWNESS, 1500, 5), new StatusEffectInstance(StatusEffects.RESISTANCE, 1500, 3)}));
        SUPER_LONG_WATER_BREATHING = register("super_long_water_breathing", new Potion("water_breathing", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.WATER_BREATHING, 36000)}));
        SUPER_LONG_POISON = register("super_long_poison", new Potion("poison", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.POISON, 6750)}));
        SUPER_STRONG_POISON = register("super_strong_poison", new Potion("poison", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.POISON, 4320, 1)}));
        SUPER_LONG_REGENERATION = register("super_long_regeneration", new Potion("regeneration", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.REGENERATION, 6750)}));
        SUPER_STRONG_REGENERATION = register("super_strong_regeneration", new Potion("regeneration", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.REGENERATION, 4500, 1)}));
        SUPER_LONG_STRENGTH = register("super_long_strength", new Potion("strength", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.STRENGTH, 36000)}));
        SUPER_STRONG_STRENGTH = register("super_strong_strength", new Potion("strength", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.STRENGTH, 18000, 1)}));
        SUPER_LONG_WEAKNESS = register("super_long_weakness", new Potion("weakness", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.WEAKNESS, 18000)}));
        SUPER_LONG_SLOW_FALLING = register("super_long_slow_falling", new Potion("slow_falling", new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.SLOW_FALLING, 18000)}));
    }
    
    @Shadow
    protected static Potion register(String string_1, Potion potion_1)
    {
        return null;
    }
    
}
