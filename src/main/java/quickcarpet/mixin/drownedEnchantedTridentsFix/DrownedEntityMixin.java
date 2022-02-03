package quickcarpet.mixin.drownedEnchantedTridentsFix;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.settings.Settings;

import java.util.Map;

@Mixin(DrownedEntity.class)
public class DrownedEntityMixin extends ZombieEntity {
    public DrownedEntityMixin(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(method = "attack", at = @At(value = "NEW", target = "net/minecraft/item/ItemStack"))
    private ItemStack quickcarpet$drownedEnchantedTridentsFix$createItemStack(ItemConvertible item) {
        if (Settings.drownedEnchantedTridentsFix) {
            ItemStack holding = this.getActiveItem();
            ItemStack trident = new ItemStack(item);
            if (holding.getItem() != Items.TRIDENT) return trident;
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(holding);
            enchantments.remove(Enchantments.LOYALTY);
            EnchantmentHelper.set(enchantments, trident);
            return trident;
        }
        return new ItemStack(item);
    }
}
