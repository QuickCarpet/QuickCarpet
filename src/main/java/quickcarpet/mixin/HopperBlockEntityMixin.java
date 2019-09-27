package quickcarpet.mixin;

import net.minecraft.block.HopperBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.annotation.BugFix;
import quickcarpet.annotation.Feature;
import quickcarpet.helper.HopperCounter;
import quickcarpet.helper.WoolTool;
import quickcarpet.settings.Settings;
import quickcarpet.utils.InventoryOptimizer;
import quickcarpet.utils.OptimizedInventory;

import javax.annotation.Nullable;

@Feature("hopperCounters")
@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin extends LootableContainerBlockEntity {

    protected HopperBlockEntityMixin(BlockEntityType<?> blockEntityType_1) {
        super(blockEntityType_1);
    }

    @Shadow public abstract double getHopperX();

    @Shadow public abstract double getHopperY();

    @Shadow public abstract double getHopperZ();

    @Shadow public abstract void setInvStack(int int_1, ItemStack itemStack_1);

    @Shadow public abstract int getInvSize();

    @Shadow private static ItemStack transfer(@Nullable Inventory from, Inventory to, ItemStack stack, int index, @Nullable Direction direction) {
        throw new AssertionError();
    }

    @Feature("hopperCounters")
    @Inject(method = "insert", at = @At("HEAD"), cancellable = true)
    private void onInsert(CallbackInfoReturnable<Boolean> cir) {
        if (Settings.hopperCounters) {
            DyeColor wool_color = WoolTool.getWoolColorAtPosition(
                    getWorld(),
                    new BlockPos(getHopperX(), getHopperY(), getHopperZ()).offset(this.getCachedState().get(HopperBlock.FACING)));


            if (wool_color != null) {
                for (int i = 0; i < this.getInvSize(); ++i) {
                    if (!this.getInvStack(i).isEmpty()) {
                        ItemStack itemstack = this.getInvStack(i);//.copy();
                        HopperCounter.COUNTERS.get(wool_color).add(this.getWorld().getServer(), itemstack);
                        this.setInvStack(i, ItemStack.EMPTY);
                    }
                }
                cir.setReturnValue(true);
            }
        }
    }

    @BugFix("blockEntityNullWorldFix")
    @Inject(method = "transfer(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/item/ItemStack;ILnet/minecraft/util/math/Direction;)Lnet/minecraft/item/ItemStack;", at = @At("HEAD"))
    private static void fixNullWorld(Inventory from, Inventory to, ItemStack stack, int i, Direction direction, CallbackInfoReturnable<ItemStack> cir) {
        if (Settings.blockEntityNullWorldFix && from instanceof BlockEntity && to instanceof BlockEntity && !((BlockEntity) to).hasWorld()) {
            BlockEntity targetBlockEntity = (BlockEntity) to;
            LogManager.getLogger().warn("BlockEntity has no world: " + BlockEntityType.getId(targetBlockEntity.getType()) + " @" + targetBlockEntity.getPos());
            targetBlockEntity.setWorld(((BlockEntity) from).getWorld());
        }
    }

    @Feature("optimizedInventories")
    @Inject(method = "transfer(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/Direction;)Lnet/minecraft/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    private static void optimizedTransfer(Inventory from, Inventory to, ItemStack stack, Direction direction, CallbackInfoReturnable<ItemStack> cir) {
        if (to instanceof OptimizedInventory) {
            OptimizedInventory optoTo = (OptimizedInventory) to;
            InventoryOptimizer optimizer = optoTo.getOptimizer();
            if (optimizer == null) return;
            while (!stack.isEmpty()) {
                int index = optimizer.findInsertSlot(stack);
                if (index == -1) break;
                int count = stack.getCount();
                stack = transfer(from, to, stack, index, direction);
                if (stack.getCount() == count) break;
            }
            cir.setReturnValue(stack);
        }
    }
}
