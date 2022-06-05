package quickcarpet.mixin.alwaysBaby;

import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.settings.Settings;

@Mixin(AnimalEntity.class)
public abstract class AnimalEntityMixin extends PassiveEntity {
    @Shadow
    protected abstract void eat(PlayerEntity player, Hand hand, ItemStack stack);

    protected AnimalEntityMixin(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "interactMob", at = @At(value = "HEAD"), cancellable = true)
    private void quickcarpet$alwaysBaby(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (Settings.alwaysBaby && !this.world.isClient && itemStack.isOf(Items.POISONOUS_POTATO) && this.isBaby() && this.breedingAge != Integer.MIN_VALUE) {
            this.setBreedingAge(Integer.MIN_VALUE);
            this.eat(player, hand, itemStack);
            world.sendEntityStatus(this, EntityStatuses.ADD_DEATH_PARTICLES);
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }

    @Override
    public void setBreedingAge(int age) {
        if (this.breedingAge == Integer.MIN_VALUE) {
            return;
        }
        super.setBreedingAge(age);
    }
}
