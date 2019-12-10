package quickcarpet.mixin.stackableShulkerBoxes;

import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.annotation.Feature;
import quickcarpet.helper.NBTHelper;
import quickcarpet.settings.Settings;

@Feature("stackableShulkerBoxes")
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    private static final int SHULKERBOX_MAX_STACK_AMOUNT = 64;

    @Shadow private int age;
    @Shadow private int pickupDelay;

    public ItemEntityMixin(EntityType<?> entityType_1, World world_1) {
        super(entityType_1, world_1);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V", at = @At("RETURN"))
    private void removeEmptyShulkerBoxTags(World worldIn, double x, double y, double z, ItemStack stack, CallbackInfo ci) {
        if (Settings.stackableShulkerBoxes && stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof ShulkerBoxBlock) {
            if (NBTHelper.cleanUpShulkerBoxTag(stack)) {
                ((ItemEntity) (Object) this).setStack(stack);
            }
        }
    }

    @Redirect(method = "canMerge", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxCount()I"))
    private int getItemStackMaxAmount(ItemStack stack) {
        if (Settings.stackableShulkerBoxes && stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof ShulkerBoxBlock)
            return SHULKERBOX_MAX_STACK_AMOUNT;

        return stack.getMaxCount();
    }

    @Inject(method = "tryMerge(Lnet/minecraft/entity/ItemEntity;)V", at = @At("HEAD"), cancellable = true)
    private void tryStackShulkerBoxes(ItemEntity other, CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        ItemStack selfStack = self.getStack();
        if (!Settings.stackableShulkerBoxes || !(selfStack.getItem() instanceof BlockItem) || !(((BlockItem) selfStack.getItem()).getBlock() instanceof ShulkerBoxBlock)) {
            return;
        }

        ItemStack otherStack = other.getStack();
        if (selfStack.getItem() == otherStack.getItem() && !NBTHelper.hasShulkerBoxItems(selfStack) && selfStack.hasTag() == otherStack.hasTag() && selfStack.getCount() + otherStack.getCount() <= SHULKERBOX_MAX_STACK_AMOUNT) {
            int amount = Math.min(otherStack.getCount(), SHULKERBOX_MAX_STACK_AMOUNT - selfStack.getCount());

            selfStack.increment(amount);
            self.setStack(selfStack);

            this.pickupDelay = Math.max(((ItemEntityMixin) (Object) other).pickupDelay, this.pickupDelay);
            this.age = Math.min(((ItemEntityMixin) (Object) other).age, this.age);

            otherStack.decrement(amount);
            if (otherStack.isEmpty()) {
                other.remove();
            } else {
                other.setStack(otherStack);
            }
            ci.cancel();
        }
    }

}
