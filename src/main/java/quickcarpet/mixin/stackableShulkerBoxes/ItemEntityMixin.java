package quickcarpet.mixin.stackableShulkerBoxes;

import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.api.annotation.Feature;
import quickcarpet.helper.NBTHelper;
import quickcarpet.settings.Settings;

@Feature("stackableShulkerBoxes")
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    private static final int SHULKERBOX_MAX_STACK_AMOUNT = 64;

    public ItemEntityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V", at = @At("RETURN"))
    private void removeEmptyShulkerBoxTags(World worldIn, double x, double y, double z, ItemStack stack, CallbackInfo ci) {
        if (Settings.stackableShulkerBoxes && stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof ShulkerBoxBlock) {
            if (NBTHelper.cleanUpShulkerBoxTag(stack)) {
                ((ItemEntity) (Object) this).setStack(stack);
            }
        }
    }

    @Redirect(method = "canMerge()Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxCount()I"))
    private int getItemStackMaxAmount1(ItemStack stack) {
        return getMaxCount(stack);
    }

    @Redirect(method = {
        "canMerge(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z",
        "merge(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;I)Lnet/minecraft/item/ItemStack;"
    }, at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxCount()I"))
    private static int getItemStackMaxAmount2(ItemStack stack) {
        return getMaxCount(stack);
    }

    private static int getMaxCount(ItemStack stack) {
        if (Settings.stackableShulkerBoxes && NBTHelper.isEmptyShulkerBox(stack)) return SHULKERBOX_MAX_STACK_AMOUNT;
        return stack.getMaxCount();
    }

    @Redirect(method = "tryMerge(Lnet/minecraft/entity/ItemEntity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;canMerge(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z"))
    private boolean canMergeModified(ItemStack a, ItemStack b) {
        if (!Settings.stackableShulkerBoxes || !NBTHelper.isEmptyShulkerBox(a) || !NBTHelper.isEmptyShulkerBox(b)) {
            return ItemEntity.canMerge(a, b);
        }
        if (a.getItem() != b.getItem()) return false;
        if (a.getCount() >= SHULKERBOX_MAX_STACK_AMOUNT || b.getCount() >= SHULKERBOX_MAX_STACK_AMOUNT) return false;
        if (a.hasTag() != b.hasTag()) return false;
        return !b.hasTag() || b.getTag().equals(a.getTag());
    }
}
