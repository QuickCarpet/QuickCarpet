package quickcarpet.mixin;

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
import quickcarpet.helper.InventoryHelper;
import quickcarpet.settings.Settings;
import quickcarpet.utils.IItemEntity;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity extends Entity implements IItemEntity
{
    private static final int SHULKERBOX_MAX_STACK_AMOUNT = 64;
    
    @Shadow
    private int age;
    @Shadow
    private int pickupDelay;
    
    public MixinItemEntity(EntityType<?> entityType_1, World world_1)
    {
        super(entityType_1, world_1);
    }
    
    @Override
    public int getPickupDelay()
    {
        return this.pickupDelay;
    }
    
    @Inject(method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V", at = @At("RETURN"))
    private void removeEmptyShulkerBoxTags(World worldIn, double x, double y, double z, ItemStack stack, CallbackInfo ci)
    {
        if (Settings.stackableShulkerBoxes && stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof ShulkerBoxBlock)
        {
            if (InventoryHelper.cleanUpShulkerBoxTag(stack))
            {
                ((ItemEntity) (Object) this).setStack(stack);
            }
        }
    }
    
    @Redirect(method = "method_20397", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxAmount()I"))
    private int getItemStackMaxAmount(ItemStack stack)
    {
        if (Settings.stackableShulkerBoxes && stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof ShulkerBoxBlock)
            return SHULKERBOX_MAX_STACK_AMOUNT;
        
        return stack.getMaxAmount();
    }
    
    @Inject(method = "tryMerge(Lnet/minecraft/entity/ItemEntity;)V", at = @At("HEAD"), cancellable = true)
    private void tryStackShulkerBoxes(ItemEntity other, CallbackInfo ci)
    {
        ItemEntity self = (ItemEntity) (Object) this;
        ItemStack selfStack = self.getStack();
        if (!Settings.stackableShulkerBoxes || !(selfStack.getItem() instanceof BlockItem) || !(((BlockItem) selfStack.getItem()).getBlock() instanceof ShulkerBoxBlock))
        {
            return;
        }
        
        ItemStack otherStack = other.getStack();
        if (selfStack.getItem() == otherStack.getItem() && !InventoryHelper.shulkerBoxHasItems(selfStack) && selfStack.hasTag() == otherStack.hasTag() && selfStack.getAmount() + otherStack.getAmount() <= SHULKERBOX_MAX_STACK_AMOUNT)
        {
            int amount = Math.min(otherStack.getAmount(), SHULKERBOX_MAX_STACK_AMOUNT - selfStack.getAmount());
            
            selfStack.addAmount(amount);
            self.setStack(selfStack);
            
            this.pickupDelay = Math.max(((IItemEntity) other).getPickupDelay(), this.pickupDelay);
            this.age = Math.min(((MixinItemEntity) (Object) other).age, this.age);
            
            otherStack.subtractAmount(amount);
            if (otherStack.isEmpty())
            {
                other.remove();
            }
            else
            {
                other.setStack(otherStack);
            }
            ci.cancel();
        }
    }
    
}
